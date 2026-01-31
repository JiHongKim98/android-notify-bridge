package com.example.kakaodiscord

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class KakaoNotificationListenerService : NotificationListenerService() {

    private val notificationCache = NotificationCache()

    companion object {
        private const val TAG = "KakaoNotification"
        private const val KAKAO_PACKAGE = "com.kakao.talk"
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return

        if (sbn.packageName != KAKAO_PACKAGE) return

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        val prefs = PreferenceManager(this)
        val targetRoom = prefs.roomName
        val targetSender = prefs.senderName
        val webhookUrl = prefs.webhookUrl

        if (targetRoom.isEmpty() || targetSender.isEmpty() || webhookUrl.isEmpty()) {
            return
        }

        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val subText = extras.getString(Notification.EXTRA_SUB_TEXT) ?: ""

        Log.d(TAG, "Notification - Title: $title, Text: $text, SubText: $subText")

        val roomName = extractRoomName(title, subText)
        val sender = extractSender(title, text)
        val message = text

        if (!matchesRoom(roomName, targetRoom)) {
            Log.d(TAG, "Room not matched: $roomName vs $targetRoom")
            return
        }

        if (!matchesSender(sender, title, text, targetSender)) {
            Log.d(TAG, "Sender not matched: $sender vs $targetSender")
            return
        }

        val cacheKey = NotificationCache.createKey(roomName, sender, message)
        if (notificationCache.isDuplicate(cacheKey)) {
            Log.d(TAG, "Duplicate notification ignored")
            return
        }

        Log.d(TAG, "Sending to Discord: [$roomName] $sender - $message")
        DiscordWebhookSender.send(webhookUrl, roomName, sender, message)
    }

    private fun extractRoomName(title: String, subText: String): String {
        return if (subText.isNotEmpty()) subText else title
    }

    private fun extractSender(title: String, text: String): String {
        return title
    }

    private fun matchesRoom(roomName: String, target: String): Boolean {
        return roomName.contains(target, ignoreCase = true)
    }

    private fun matchesSender(sender: String, title: String, text: String, target: String): Boolean {
        return sender.contains(target, ignoreCase = true) ||
                title.contains(target, ignoreCase = true)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // 알림 제거 시 특별한 처리 없음
    }
}
