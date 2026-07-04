package com.uopeople.bugtracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "issues")
data class IssueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val remoteId: Long? = null,
    val title: String,
    val description: String,
    val priority: Priority,
    val status: IssueStatus,
    val createdAt: Long,
    val syncStatus: SyncStatus = SyncStatus.PENDING_CREATE,
    val retryCount: Int = 0,
    val lastSyncAttempt: Long? = null,
    val syncErrorMessage: String? = null
)
