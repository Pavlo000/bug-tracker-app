package com.uopeople.bugtracker.data.remote

import com.uopeople.bugtracker.data.local.IssueStatus
import com.uopeople.bugtracker.data.local.Priority

data class IssueDto(
    val id: Long? = null,
    val title: String,
    val description: String,
    val priority: String,
    val status: String,
    val createdAt: Long
) {
    fun toPriority(): Priority = Priority.valueOf(priority)
    fun toStatus(): IssueStatus = IssueStatus.valueOf(status)
}

data class CreateIssueRequest(
    val title: String,
    val description: String,
    val priority: String,
    val status: String,
    val createdAt: Long
)
