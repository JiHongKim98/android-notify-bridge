package com.example.kakaodiscord.ui.screen

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kakaodiscord.R
import com.example.kakaodiscord.data.NotificationRule
import com.example.kakaodiscord.data.RuleRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleEditScreen(
    existingRule: NotificationRule?,
    onSave: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val repository = remember { RuleRepository(context) }

    var selectedAppPackage by remember { mutableStateOf(existingRule?.appPackage ?: "") }
    var selectedAppName by remember { mutableStateOf(existingRule?.appName ?: "") }
    var roomKeyword by remember { mutableStateOf(existingRule?.roomKeyword ?: "") }
    var senderKeyword by remember { mutableStateOf(existingRule?.senderKeyword ?: "") }
    var keyword by remember { mutableStateOf(existingRule?.keyword ?: "") }
    var webhookUrl by remember { mutableStateOf(existingRule?.webhookUrl ?: "") }
    var showAppPicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isEditMode = existingRule != null
    val titleRes = if (isEditMode) R.string.edit_rule else R.string.add_rule

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(titleRes)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // App Selector
            OutlinedTextField(
                value = if (selectedAppName.isNotEmpty()) {
                    "$selectedAppName\n$selectedAppPackage"
                } else {
                    ""
                },
                onValueChange = {},
                label = { Text(stringResource(R.string.target_app)) },
                placeholder = { Text(stringResource(R.string.select_app)) },
                readOnly = true,
                enabled = false,
                minLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAppPicker = true },
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.tap_to_select_app),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAppPicker = true }
                    .padding(vertical = 8.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Filter Section Header
            Text(
                text = stringResource(R.string.filter_section),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Room Keyword Input (Optional)
            OutlinedTextField(
                value = roomKeyword,
                onValueChange = { 
                    roomKeyword = it
                    errorMessage = null
                },
                label = { Text(stringResource(R.string.hint_room_keyword)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = stringResource(R.string.room_keyword_help),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            )

            // Sender Keyword Input (Optional)
            OutlinedTextField(
                value = senderKeyword,
                onValueChange = { 
                    senderKeyword = it
                    errorMessage = null
                },
                label = { Text(stringResource(R.string.hint_sender_keyword)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = stringResource(R.string.sender_keyword_help),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            )

            // Content Keyword Input (Optional)
            OutlinedTextField(
                value = keyword,
                onValueChange = { 
                    keyword = it
                    errorMessage = null
                },
                label = { Text(stringResource(R.string.hint_keyword)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = stringResource(R.string.keyword_help),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )

            Text(
                text = stringResource(R.string.filter_empty_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
            )

            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Webhook URL Input
            OutlinedTextField(
                value = webhookUrl,
                onValueChange = { 
                    webhookUrl = it
                    errorMessage = null
                },
                label = { Text(stringResource(R.string.hint_webhook_url)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Error Message
            errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }

            // Save Button
            Button(
                onClick = {
                    when {
                        selectedAppPackage.isEmpty() -> {
                            errorMessage = context.getString(R.string.error_select_app)
                        }
                        webhookUrl.isBlank() -> {
                            errorMessage = context.getString(R.string.error_enter_webhook)
                        }
                        !webhookUrl.startsWith("https://") -> {
                            errorMessage = context.getString(R.string.error_invalid_webhook)
                        }
                        else -> {
                            val rule = NotificationRule(
                                id = existingRule?.id ?: java.util.UUID.randomUUID().toString(),
                                appPackage = selectedAppPackage,
                                appName = selectedAppName,
                                keyword = keyword.trim(),
                                senderKeyword = senderKeyword.trim(),
                                roomKeyword = roomKeyword.trim(),
                                webhookUrl = webhookUrl.trim(),
                                isEnabled = existingRule?.isEnabled ?: true,
                            )
                            if (isEditMode) {
                                repository.updateRule(rule)
                            } else {
                                repository.addRule(rule)
                            }
                            onSave()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.btn_save))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showAppPicker) {
        AppPickerDialog(
            onDismiss = { showAppPicker = false },
            onAppSelected = { selection ->
                selectedAppPackage = selection.packageName
                selectedAppName = selection.appName
                showAppPicker = false
                errorMessage = null
            },
        )
    }
}
