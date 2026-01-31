package com.example.kakaodiscord

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.kakaodiscord.data.NotificationRule
import com.example.kakaodiscord.data.RuleRepository

class NotificationListenerService : NotificationListenerService() {

    private val notificationCache = NotificationCache()

    companion object {
        private const val TAG = "NotifyBridge"
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return
        val packageName = sbn.packageName ?: return

        val repository = RuleRepository(this)
        val enabledRules = repository.getEnabledRules()

        if (enabledRules.isEmpty()) {
            return
        }

        // Find matching rules for this package
        val matchingRules = enabledRules.filter { it.appPackage == packageName }
        if (matchingRules.isEmpty()) {
            return
        }

        // Extract notification fields
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val subText = extras.getString(Notification.EXTRA_SUB_TEXT) ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""

        // Sender is typically in the title
        val sender = title
        
        // Room/chat name is typically in subText (for group chats)
        // For 1:1 chats, subText is usually empty
        val room = subText
        
        // Full content for keyword matching
        val fullContent = "$title $text $subText $bigText"

        Log.d(TAG, "Notification from $packageName - Title: $title, Text: $text, SubText: $subText")

        for (rule in matchingRules) {
            // Check room filter (if specified)
            if (rule.roomKeyword.isNotEmpty() && !matchesFilter(room, rule.roomKeyword)) {
                Log.d(TAG, "Room not matched: '$room' vs '${rule.roomKeyword}'")
                continue
            }

            // Check sender filter (if specified)
            if (rule.senderKeyword.isNotEmpty() && !matchesFilter(sender, rule.senderKeyword)) {
                Log.d(TAG, "Sender not matched: '$sender' vs '${rule.senderKeyword}'")
                continue
            }

            // Check content keyword filter (if specified)
            if (rule.keyword.isNotEmpty() && !matchesFilter(fullContent, rule.keyword)) {
                Log.d(TAG, "Keyword not matched in content")
                continue
            }

            // Duplicate check
            val cacheKey = NotificationCache.createKey(rule.id, title, text)
            if (notificationCache.isDuplicate(cacheKey)) {
                Log.d(TAG, "Duplicate notification ignored for rule: ${rule.appName}")
                continue
            }

            Log.d(TAG, "Sending to Discord: [${rule.appName}] $sender - $text (room: $room)")
            sendToDiscord(rule, sender, room, text)
        }
    }

    private fun matchesFilter(content: String, keyword: String): Boolean {
        return content.contains(keyword, ignoreCase = true)
    }

    private fun sendToDiscord(rule: NotificationRule, sender: String, room: String, message: String) {
        val displaySender = sender.ifEmpty { rule.appName }
        val displayRoom = room.ifEmpty { rule.appName }
        
        DiscordWebhookSender.send(
            webhookUrl = rule.webhookUrl,
            roomName = displayRoom,
            sender = displaySender,
            message = message,
        )
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // No action needed
    }
}
