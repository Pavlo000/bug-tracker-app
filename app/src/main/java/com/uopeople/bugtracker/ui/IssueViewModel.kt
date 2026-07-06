package com.uopeople.bugtracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uopeople.bugtracker.BugTrackerApp
import com.uopeople.bugtracker.data.local.IssueEntity
import com.uopeople.bugtracker.data.local.IssueStatus
import com.uopeople.bugtracker.data.local.Priority
import com.uopeople.bugtracker.data.local.SyncStatus
import com.uopeople.bugtracker.data.repository.SyncResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IssueViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as BugTrackerApp
    private val repository = app.repository

    val issues: StateFlow<List<IssueEntity>> = repository.getAllIssues()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isOnline: StateFlow<Boolean> = app.networkMonitor.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    private val _selectedIssue = MutableStateFlow<IssueEntity?>(null)
    val selectedIssue: StateFlow<IssueEntity?> = _selectedIssue.asStateFlow()

    fun selectIssue(issue: IssueEntity?) {
        _selectedIssue.value = issue
    }

    fun createIssue(
        title: String,
        description: String,
        priority: Priority,
        status: IssueStatus
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.createIssue(title.trim(), description.trim(), priority, status)
            app.syncManager.requestManualSync()
        }
    }

    fun updateIssue(issue: IssueEntity) {
        viewModelScope.launch {
            repository.updateIssue(issue)
            app.syncManager.requestManualSync()
        }
    }

    fun deleteIssue(issue: IssueEntity) {
        viewModelScope.launch {
            repository.deleteIssue(issue)
            app.syncManager.requestManualSync()
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            if (!isOnline.value) {
                _syncMessage.value = "Offline — changes saved locally and will sync when online."
                return@launch
            }
            try {
                val result = repository.syncPendingIssues()
                _syncMessage.value = formatSyncMessage(result)
            } catch (e: Exception) {
                _syncMessage.value = "Sync failed: ${e.message}"
            }
        }
    }

    fun clearSyncMessage() {
        _syncMessage.value = null
    }

    private fun formatSyncMessage(result: SyncResult): String {
        return when {
            result.attempted == 0 -> "Everything is up to date."
            result.isSuccess -> "Synced ${result.succeeded} issue(s) successfully."
            else -> "Synced ${result.succeeded}/${result.attempted}. ${result.failed} failed — will retry automatically."
        }
    }
}

fun SyncStatus.displayLabel(): String = when (this) {
    SyncStatus.SYNCED -> "Synced"
    SyncStatus.PENDING_CREATE -> "Pending upload"
    SyncStatus.PENDING_UPDATE -> "Pending update"
    SyncStatus.PENDING_DELETE -> "Pending delete"
    SyncStatus.SYNC_FAILED -> "Sync failed"
}

fun Priority.displayLabel(): String = name.lowercase().replaceFirstChar { it.uppercase() }

fun IssueStatus.displayLabel(): String = when (this) {
    IssueStatus.IN_PROGRESS -> "In Progress"
    else -> name.lowercase().replaceFirstChar { it.uppercase() }
}
