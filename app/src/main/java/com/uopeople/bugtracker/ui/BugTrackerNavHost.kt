package com.uopeople.bugtracker.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

object Routes {
    const val LIST = "list"
    const val CREATE = "create"
    const val EDIT = "edit/{issueId}"

    fun edit(issueId: Long) = "edit/$issueId"
}

@Composable
fun BugTrackerNavHost(viewModel: IssueViewModel) {
    val navController = rememberNavController()
    val issues by viewModel.issues.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val syncMessage by viewModel.syncMessage.collectAsState()

    NavHost(navController = navController, startDestination = Routes.LIST) {
        composable(Routes.LIST) {
            IssueListScreen(
                issues = issues,
                isOnline = isOnline,
                syncMessage = syncMessage,
                onSyncNow = viewModel::syncNow,
                onDismissSyncMessage = viewModel::clearSyncMessage,
                onCreateClick = { navController.navigate(Routes.CREATE) },
                onIssueClick = { issue ->
                    navController.navigate(Routes.edit(issue.id))
                },
                onDeleteIssue = viewModel::deleteIssue
            )
        }

        composable(Routes.CREATE) {
            IssueFormScreen(
                title = "New Issue",
                onSave = { title, description, priority, status ->
                    viewModel.createIssue(title, description, priority, status)
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.EDIT,
            arguments = listOf(navArgument("issueId") { type = NavType.LongType })
        ) { backStackEntry ->
            val issueId = backStackEntry.arguments?.getLong("issueId") ?: return@composable
            val issue = issues.find { it.id == issueId }
            if (issue == null) {
                navController.popBackStack()
                return@composable
            }
            IssueFormScreen(
                title = "Edit Issue",
                initialIssue = issue,
                onSave = { title, description, priority, status ->
                    viewModel.updateIssue(
                        issue.copy(
                            title = title,
                            description = description,
                            priority = priority,
                            status = status
                        )
                    )
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        }
    }
}
