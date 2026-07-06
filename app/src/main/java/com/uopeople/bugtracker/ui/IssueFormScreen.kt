package com.uopeople.bugtracker.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uopeople.bugtracker.data.local.IssueEntity
import com.uopeople.bugtracker.data.local.IssueStatus
import com.uopeople.bugtracker.data.local.Priority

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueFormScreen(
    title: String,
    initialIssue: IssueEntity? = null,
    onSave: (String, String, Priority, IssueStatus) -> Unit,
    onCancel: () -> Unit
) {
    var issueTitle by remember { mutableStateOf(initialIssue?.title ?: "") }
    var description by remember { mutableStateOf(initialIssue?.description ?: "") }
    var priority by remember { mutableStateOf(initialIssue?.priority ?: Priority.MEDIUM) }
    var status by remember { mutableStateOf(initialIssue?.status ?: IssueStatus.OPEN) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = issueTitle,
                onValueChange = { issueTitle = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                minLines = 4
            )
            Spacer(modifier = Modifier.height(12.dp))
            EnumDropdown(
                label = "Priority",
                options = Priority.entries,
                selected = priority,
                onSelected = { priority = it },
                display = { it.displayLabel() }
            )
            Spacer(modifier = Modifier.height(12.dp))
            EnumDropdown(
                label = "Status",
                options = IssueStatus.entries,
                selected = status,
                onSelected = { status = it },
                display = { it.displayLabel() }
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    onSave(issueTitle, description, priority, status)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = issueTitle.isNotBlank()
            ) {
                Text("Save")
            }
            TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> EnumDropdown(
    label: String,
    options: List<T>,
    selected: T,
    onSelected: (T) -> Unit,
    display: (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = display(selected),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(display(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
