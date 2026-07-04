package com.uopeople.bugtracker

import android.app.Application
import androidx.room.Room
import com.uopeople.bugtracker.data.local.AppDatabase
import com.uopeople.bugtracker.data.remote.RetrofitClient
import com.uopeople.bugtracker.data.repository.IssueRepository
import com.uopeople.bugtracker.sync.NetworkMonitor
import com.uopeople.bugtracker.sync.SyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class BugTrackerApp : Application() {

    lateinit var repository: IssueRepository
        private set

    lateinit var syncManager: SyncManager
        private set

    lateinit var networkMonitor: NetworkMonitor
        private set

    private val applicationScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()

        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "bug_tracker.db"
        ).build()

        repository = IssueRepository(
            issueDao = database.issueDao(),
            api = RetrofitClient.api
        )

        networkMonitor = NetworkMonitor(applicationContext)
        syncManager = SyncManager(
            context = applicationContext,
            repository = repository,
            networkMonitor = networkMonitor,
            scope = applicationScope
        )
        syncManager.start()
    }
}
