package com.example.notifybridge.ui.screen

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.notifybridge.R

data class AppSelection(
    val packageName: String,
    val appName: String,
)

@Composable
fun AppPickerDialog(
    onDismiss: () -> Unit,
    onAppSelected: (AppSelection) -> Unit,
) {
    var showManualInput by remember { mutableStateOf(false) }

    if (showManualInput) {
        ManualPackageInputDialog(
            onDismiss = { showManualInput = false },
            onConfirm = { packageName, appName ->
                onAppSelected(AppSelection(packageName, appName))
            },
        )
    } else {
        AppListDialog(
            onDismiss = onDismiss,
            onAppSelected = onAppSelected,
            onManualInput = { showManualInput = true },
        )
    }
}

@Composable
private fun AppListDialog(
    onDismiss: () -> Unit,
    onAppSelected: (AppSelection) -> Unit,
    onManualInput: () -> Unit,
) {
    val context = LocalContext.current
    val packageManager = context.packageManager

    var searchQuery by remember { mutableStateOf("") }

    val installedApps =
        remember {
            packageManager
                .getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { appInfo ->
                    // Show apps that are not pure system apps (user-installed or updated system apps)
                    (appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) ||
                        (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0)
                }.sortedBy { packageManager.getApplicationLabel(it).toString().lowercase() }
        }

    val filteredApps =
        remember(searchQuery) {
            if (searchQuery.isEmpty()) {
                installedApps
            } else {
                installedApps.filter { appInfo ->
                    val appName = packageManager.getApplicationLabel(appInfo).toString()
                    appName.contains(searchQuery, ignoreCase = true) ||
                        appInfo.packageName.contains(searchQuery, ignoreCase = true)
                }
            }
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_app)) },
        text = {
            Column {
                // Manual Input Button
                OutlinedButton(
                    onClick = onManualInput,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.manual_input))
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(stringResource(R.string.search_app)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.height(350.dp),
                ) {
                    items(filteredApps) { appInfo ->
                        AppItem(
                            appInfo = appInfo,
                            packageManager = packageManager,
                            onClick = {
                                val appName = packageManager.getApplicationLabel(appInfo).toString()
                                onAppSelected(AppSelection(appInfo.packageName, appName))
                            },
                        )
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        },
    )
}

@Composable
private fun ManualPackageInputDialog(
    onDismiss: () -> Unit,
    onConfirm: (packageName: String, appName: String) -> Unit,
) {
    var packageName by remember { mutableStateOf("") }
    var appName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.manual_package_input)) },
        text = {
            Column {
                OutlinedTextField(
                    value = packageName,
                    onValueChange = {
                        packageName = it
                        errorMessage = null
                    },
                    label = { Text(stringResource(R.string.hint_package_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = appName,
                    onValueChange = {
                        appName = it
                        errorMessage = null
                    },
                    label = { Text(stringResource(R.string.hint_app_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        packageName.isBlank() -> {
                            errorMessage = context.getString(R.string.error_empty_package)
                        }
                        appName.isBlank() -> {
                            errorMessage = context.getString(R.string.error_empty_app_name)
                        }
                        else -> {
                            onConfirm(packageName.trim(), appName.trim())
                        }
                    }
                },
            ) {
                Text(stringResource(R.string.btn_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        },
    )
}

@Composable
private fun AppItem(
    appInfo: ApplicationInfo,
    packageManager: PackageManager,
    onClick: () -> Unit,
) {
    val appName = remember { packageManager.getApplicationLabel(appInfo).toString() }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = appName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = appInfo.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
