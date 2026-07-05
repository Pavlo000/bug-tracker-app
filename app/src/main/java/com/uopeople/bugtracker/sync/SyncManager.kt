package com.uopeople.bugtracker.sync

import android.content.Context
import com.uopeople.bugtracker.data.repository.IssueRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SyncManager(
    private val context: Context,
    private val repository: IssueRepository,
    private val networkMonitor: NetworkMonitor,
    private val scope: CoroutineScope
) {

    fun start() {
        networkMonitor.startMonitoring()
        scope.launch {
            networkMonitor.isOnline.collect { online ->
                if (online) {
                    SyncWorker.enqueue(context)
                }
            }
        }
    }

    fun requestManualSync() {
        SyncWorker.enqueue(context)
    }

    fun stop() {
        networkMonitor.stopMonitoring()
    }
}
