package com.example.kakaodiscord

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.kakaodiscord.ui.theme.DiscordGreen
import com.example.kakaodiscord.ui.theme.DiscordRed
import com.example.kakaodiscord.ui.theme.NotifyBridgeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotifyBridgeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val componentName = ComponentName(this, KakaoNotificationListenerService::class.java)
        val enabledListeners = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        ) ?: return false
        return enabledListeners.contains(componentName.flattenToString())
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val prefs = remember { PreferenceManager(context) }

    var webhookUrl by remember { mutableStateOf(prefs.webhookUrl) }
    var roomName by remember { mutableStateOf(prefs.roomName) }
    var senderName by remember { mutableStateOf(prefs.senderName) }
    var isPermissionGranted by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isPermissionGranted = checkNotificationListenerEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Permission Status
        Text(
            text = stringResource(
                if (isPermissionGranted) R.string.status_permission_granted
                else R.string.status_permission_denied
            ),
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isPermissionGranted) DiscordGreen else DiscordRed)
                .padding(12.dp),
            textAlign = TextAlign.Center,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Permission Button
        Button(
            onClick = {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            },
            enabled = !isPermissionGranted,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.btn_permission))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Webhook URL Input
        OutlinedTextField(
            value = webhookUrl,
            onValueChange = { webhookUrl = it },
            label = { Text(stringResource(R.string.hint_webhook_url)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Room Name Input
        OutlinedTextField(
            value = roomName,
            onValueChange = { roomName = it },
            label = { Text(stringResource(R.string.hint_room_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Sender Name Input
        OutlinedTextField(
            value = senderName,
            onValueChange = { senderName = it },
            label = { Text(stringResource(R.string.hint_sender_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Save Button
        Button(
            onClick = {
                if (webhookUrl.isBlank() || roomName.isBlank() || senderName.isBlank()) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.toast_fill_all),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    prefs.webhookUrl = webhookUrl.trim()
                    prefs.roomName = roomName.trim()
                    prefs.senderName = senderName.trim()
                    Toast.makeText(
                        context,
                        context.getString(R.string.toast_saved),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.btn_save))
        }
    }
}

private fun checkNotificationListenerEnabled(context: android.content.Context): Boolean {
    val componentName = ComponentName(context, KakaoNotificationListenerService::class.java)
    val enabledListeners = Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners"
    ) ?: return false
    return enabledListeners.contains(componentName.flattenToString())
}
