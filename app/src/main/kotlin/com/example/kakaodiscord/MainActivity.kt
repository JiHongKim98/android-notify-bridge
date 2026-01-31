package com.example.kakaodiscord

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var tvPermissionStatus: TextView
    private lateinit var btnPermission: Button
    private lateinit var etWebhookUrl: TextInputEditText
    private lateinit var etRoomName: TextInputEditText
    private lateinit var etSenderName: TextInputEditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        loadSettings()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }

    private fun initViews() {
        tvPermissionStatus = findViewById(R.id.tvPermissionStatus)
        btnPermission = findViewById(R.id.btnPermission)
        etWebhookUrl = findViewById(R.id.etWebhookUrl)
        etRoomName = findViewById(R.id.etRoomName)
        etSenderName = findViewById(R.id.etSenderName)
        btnSave = findViewById(R.id.btnSave)
    }

    private fun loadSettings() {
        val prefs = PreferenceManager(this)
        etWebhookUrl.setText(prefs.webhookUrl)
        etRoomName.setText(prefs.roomName)
        etSenderName.setText(prefs.senderName)
    }

    private fun setupListeners() {
        btnPermission.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        btnSave.setOnClickListener {
            saveSettings()
        }
    }

    private fun saveSettings() {
        val webhookUrl = etWebhookUrl.text?.toString()?.trim() ?: ""
        val roomName = etRoomName.text?.toString()?.trim() ?: ""
        val senderName = etSenderName.text?.toString()?.trim() ?: ""

        if (webhookUrl.isEmpty() || roomName.isEmpty() || senderName.isEmpty()) {
            Toast.makeText(this, R.string.toast_fill_all, Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = PreferenceManager(this)
        prefs.webhookUrl = webhookUrl
        prefs.roomName = roomName
        prefs.senderName = senderName

        Toast.makeText(this, R.string.toast_saved, Toast.LENGTH_SHORT).show()
    }

    private fun updatePermissionStatus() {
        val isEnabled = isNotificationListenerEnabled()
        tvPermissionStatus.setText(
            if (isEnabled) R.string.status_permission_granted
            else R.string.status_permission_denied
        )
        tvPermissionStatus.setBackgroundColor(
            if (isEnabled) 0xFF57F287.toInt() else 0xFFED4245.toInt()
        )
        btnPermission.isEnabled = !isEnabled
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
