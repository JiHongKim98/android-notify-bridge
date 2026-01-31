package com.example.notifybridge.data

import android.content.Context
import android.content.SharedPreferences

class RuleRepository(
    context: Context,
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE,
        )

    fun getRules(): List<NotificationRule> {
        val json = prefs.getString(KEY_RULES, "") ?: ""
        return NotificationRule.listFromJson(json)
    }

    fun saveRules(rules: List<NotificationRule>) {
        prefs
            .edit()
            .putString(KEY_RULES, NotificationRule.listToJson(rules))
            .apply()
    }

    fun addRule(rule: NotificationRule) {
        val rules = getRules().toMutableList()
        rules.add(rule)
        saveRules(rules)
    }

    fun updateRule(rule: NotificationRule) {
        val rules = getRules().toMutableList()
        val index = rules.indexOfFirst { it.id == rule.id }
        if (index != -1) {
            rules[index] = rule
            saveRules(rules)
        }
    }

    fun deleteRule(ruleId: String) {
        val rules = getRules().filter { it.id != ruleId }
        saveRules(rules)
    }

    fun getEnabledRules(): List<NotificationRule> = getRules().filter { it.isEnabled }

    companion object {
        private const val PREF_NAME = "notification_rules"
        private const val KEY_RULES = "rules"
    }
}
