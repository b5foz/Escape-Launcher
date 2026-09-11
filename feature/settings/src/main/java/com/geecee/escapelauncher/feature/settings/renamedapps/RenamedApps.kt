package com.geecee.escapelauncher.feature.settings.renamedapps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.EscapeHeader
import com.geecee.escapelauncher.core.ui.composables.SettingsButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun RenamedApps(
    onBackClicked: () -> Unit,
    viewModel: RenamedAppsViewModel
) {
    val renamedApps by viewModel.renamedApps.collectAsState()
    val lazyListState = rememberLazyListState()
    var selectedAppForRename by remember { mutableStateOf<InstalledApp?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            item(key = "header") {
                EscapeHeader(
                    goBack = onBackClicked,
                    title = stringResource(R.string.manage_renamed_apps),
                    padding = true
                )
            }

            if (renamedApps.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No renamed apps",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(
                    items = renamedApps,
                    key = { it.packageName }
                ) { app ->
                    val isTopOfGroup = app.packageName == renamedApps.first().packageName
                    val isBottomOfGroup = app.packageName == renamedApps.last().packageName

                    SettingsButton(
                        label = app.displayName,
                        onClick = {
                            selectedAppForRename = app
                        },
                        isTopOfGroup = isTopOfGroup,
                        isBottomOfGroup = isBottomOfGroup
                    )
                }
            }
        }

        if (selectedAppForRename != null) {
            RenameAppDialog(
                app = selectedAppForRename!!,
                onDismiss = { selectedAppForRename = null },
                onConfirm = { newName ->
                    viewModel.updateRename(selectedAppForRename!!.packageName, newName)
                    selectedAppForRename = null
                }
            )
        }
    }
}

@Composable
fun RenameAppDialog(
    app: InstalledApp,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(app.displayName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.rename_app)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

