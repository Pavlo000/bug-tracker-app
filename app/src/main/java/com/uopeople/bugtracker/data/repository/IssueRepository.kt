package com.uopeople.bugtracker.data.repository

import com.uopeople.bugtracker.data.local.IssueDao
import com.uopeople.bugtracker.data.local.IssueEntity
import com.uopeople.bugtracker.data.local.IssueStatus
import com.uopeople.bugtracker.data.local.Priority
import com.uopeople.bugtracker.data.local.SyncStatus
import com.uopeople.bugtracker.data.remote.CreateIssueRequest
import com.uopeople.bugtracker.data.remote.IssueApiService
import com.uopeople.bugtracker.data.remote.IssueDto
import kotlinx.coroutines.flow.Flow

class IssueRepository(
    private val issueDao: IssueDao,
    private val api: IssueApiService
) {
    companion object {
        const val MAX_RETRY_COUNT = 5
    }

    fun getAllIssues(): Flow<List<IssueEntity>> = issueDao.getAllIssues()

    suspend fun getIssueById(id: Long): IssueEntity? = issueDao.getIssueById(id)

    suspend fun createIssue(
        title: String,
        description: String,
        priority: Priority,
        status: IssueStatus = IssueStatus.OPEN
    ): Long {
        val issue = IssueEntity(
            title = title,
            description = description,
            priority = priority,
            status = status,
            createdAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING_CREATE
        )
        return issueDao.insert(issue)
    }

    suspend fun updateIssue(issue: IssueEntity) {
        val syncStatus = when (issue.syncStatus) {
            SyncStatus.PENDING_CREATE -> SyncStatus.PENDING_CREATE
            SyncStatus.PENDING_DELETE -> SyncStatus.PENDING_DELETE
            else -> SyncStatus.PENDING_UPDATE
        }
        issueDao.update(
            issue.copy(
                syncStatus = syncStatus,
                syncErrorMessage = null
            )
        )
    }

    suspend fun deleteIssue(issue: IssueEntity) {
        if (issue.remoteId == null) {
            issueDao.deleteById(issue.id)
        } else {
            issueDao.update(
                issue.copy(
                    syncStatus = SyncStatus.PENDING_DELETE,
                    syncErrorMessage = null
                )
            )
        }
    }

    suspend fun syncPendingIssues(): SyncResult {
        val pending = issueDao.getPendingSyncIssues()
        var successCount = 0
        var failureCount = 0
        val errors = mutableListOf<String>()

        for (issue in pending) {
            try {
                when (issue.syncStatus) {
                    SyncStatus.PENDING_CREATE -> pushCreate(issue)
                    SyncStatus.PENDING_UPDATE -> pushUpdate(issue)
                    SyncStatus.PENDING_DELETE -> pushDelete(issue)
                    SyncStatus.SYNC_FAILED -> retryFailedIssue(issue)
                    SyncStatus.SYNCED -> Unit
                }
                successCount++
            } catch (e: Exception) {
                failureCount++
                markSyncFailed(issue, e.message ?: "Unknown sync error")
                errors.add("${issue.title}: ${e.message}")
            }
        }

        issueDao.purgeLocalOnlyDeletes()
        pullRemoteIssues()

        return SyncResult(
            attempted = pending.size,
            succeeded = successCount,
            failed = failureCount,
            errors = errors
        )
    }

    private suspend fun retryFailedIssue(issue: IssueEntity) {
        if (issue.retryCount >= MAX_RETRY_COUNT) {
            throw IllegalStateException("Max retry attempts reached")
        }

        when {
            issue.remoteId == null -> pushCreate(issue)
            issue.syncStatus == SyncStatus.PENDING_DELETE -> pushDelete(issue)
            else -> pushUpdate(issue)
        }
    }

    private suspend fun pushCreate(issue: IssueEntity) {
        val response = api.createIssue(issue.toRequest())
        val remoteId = response.id ?: throw IllegalStateException("Server did not return an id")
        issueDao.update(
            issue.copy(
                remoteId = remoteId,
                syncStatus = SyncStatus.SYNCED,
                retryCount = 0,
                lastSyncAttempt = System.currentTimeMillis(),
                syncErrorMessage = null
            )
        )
    }

    private suspend fun pushUpdate(issue: IssueEntity) {
        val remoteId = issue.remoteId
            ?: throw IllegalStateException("Cannot update issue without remote id")
        api.updateIssue(remoteId, issue.toRequest())
        issueDao.update(
            issue.copy(
                syncStatus = SyncStatus.SYNCED,
                retryCount = 0,
                lastSyncAttempt = System.currentTimeMillis(),
                syncErrorMessage = null
            )
        )
    }

    private suspend fun pushDelete(issue: IssueEntity) {
        val remoteId = issue.remoteId
        if (remoteId != null) {
            api.deleteIssue(remoteId)
        }
        issueDao.deleteById(issue.id)
    }

    private suspend fun markSyncFailed(issue: IssueEntity, message: String) {
        issueDao.update(
            issue.copy(
                syncStatus = SyncStatus.SYNC_FAILED,
                retryCount = issue.retryCount + 1,
                lastSyncAttempt = System.currentTimeMillis(),
                syncErrorMessage = message
            )
        )
    }

    private suspend fun pullRemoteIssues() {
        val remoteIssues = api.getIssues()
        for (remote in remoteIssues) {
            mergeRemoteIssue(remote)
        }
    }

    private suspend fun mergeRemoteIssue(remote: IssueDto) {
        val remoteId = remote.id ?: return
        val existingByRemote = issueDao.getIssueByRemoteId(remoteId)
        if (existingByRemote != null) {
            if (existingByRemote.syncStatus == SyncStatus.SYNCED) {
                issueDao.update(existingByRemote.fromRemote(remote))
            }
            return
        }

        issueDao.insert(
            IssueEntity(
                remoteId = remoteId,
                title = remote.title,
                description = remote.description,
                priority = remote.toPriority(),
                status = remote.toStatus(),
                createdAt = remote.createdAt,
                syncStatus = SyncStatus.SYNCED
            )
        )
    }

    private fun IssueEntity.toRequest() = CreateIssueRequest(
        title = title,
        description = description,
        priority = priority.name,
        status = status.name,
        createdAt = createdAt
    )

    private fun IssueEntity.fromRemote(remote: IssueDto) = copy(
        title = remote.title,
        description = remote.description,
        priority = remote.toPriority(),
        status = remote.toStatus(),
        createdAt = remote.createdAt,
        syncStatus = SyncStatus.SYNCED,
        retryCount = 0,
        syncErrorMessage = null
    )
}

data class SyncResult(
    val attempted: Int,
    val succeeded: Int,
    val failed: Int,
    val errors: List<String>
) {
    val isSuccess: Boolean get() = failed == 0
}
