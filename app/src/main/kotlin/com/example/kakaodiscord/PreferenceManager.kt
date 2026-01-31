package com.example.kakaodiscord

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(
    context: Context,
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE,
        )

    var webhookUrl: String
        get() = prefs.getString(KEY_WEBHOOK_URL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_WEBHOOK_URL, value).apply()

    var roomName: String
        get() = prefs.getString(KEY_ROOM_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_ROOM_NAME, value).apply()

    var senderName: String
        get() = prefs.getString(KEY_SENDER_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_SENDER_NAME, value).apply()

    companion object {
        private const val PREF_NAME = "kakao_discord_prefs"
        private const val KEY_WEBHOOK_URL = "webhook_url"
        private const val KEY_ROOM_NAME = "room_name"
        private const val KEY_SENDER_NAME = "sender_name"
    }
}
