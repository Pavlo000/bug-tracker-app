package com.uopeople.bugtracker.data.local

enum class Priority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class IssueStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED
}

enum class SyncStatus {
    SYNCED,
    PENDING_CREATE,
    PENDING_UPDATE,
    PENDING_DELETE,
    SYNC_FAILED
}
