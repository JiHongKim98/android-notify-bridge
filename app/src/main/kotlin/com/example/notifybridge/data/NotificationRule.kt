package com.example.notifybridge.data

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class NotificationRule(
    val id: String = UUID.randomUUID().toString(),
    val appPackage: String,
    val appName: String,
    val keyword: String = "", // 내용 키워드 필터 (선택)
    val senderKeyword: String = "", // 발신자 필터 (선택)
    val roomKeyword: String = "", // 채팅방 필터 (선택)
    val webhookUrl: String,
    val isEnabled: Boolean = true,
) {
    fun toJson(): JSONObject =
        JSONObject().apply {
            put("id", id)
            put("appPackage", appPackage)
            put("appName", appName)
            put("keyword", keyword)
            put("senderKeyword", senderKeyword)
            put("roomKeyword", roomKeyword)
            put("webhookUrl", webhookUrl)
            put("isEnabled", isEnabled)
        }

    companion object {
        fun fromJson(json: JSONObject): NotificationRule =
            NotificationRule(
                id = json.getString("id"),
                appPackage = json.getString("appPackage"),
                appName = json.getString("appName"),
                keyword = json.optString("keyword", ""),
                senderKeyword = json.optString("senderKeyword", ""),
                roomKeyword = json.optString("roomKeyword", ""),
                webhookUrl = json.getString("webhookUrl"),
                isEnabled = json.optBoolean("isEnabled", true),
            )

        fun listToJson(rules: List<NotificationRule>): String {
            val jsonArray = JSONArray()
            rules.forEach { rule ->
                jsonArray.put(rule.toJson())
            }
            return jsonArray.toString()
        }

        fun listFromJson(jsonString: String): List<NotificationRule> {
            if (jsonString.isEmpty()) return emptyList()
            val jsonArray = JSONArray(jsonString)
            val rules = mutableListOf<NotificationRule>()
            for (i in 0 until jsonArray.length()) {
                rules.add(fromJson(jsonArray.getJSONObject(i)))
            }
            return rules
        }
    }
}
