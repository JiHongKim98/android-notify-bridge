package com.example.notifybridge

import android.util.LruCache

class NotificationCache(
    maxSize: Int = 50,
) {
    private val cache = LruCache<String, Long>(maxSize)
    private val ttlMillis = 5000L // 5초 TTL

    @Synchronized
    fun isDuplicate(key: String): Boolean {
        val now = System.currentTimeMillis()
        val timestamp = cache.get(key)

        if (timestamp != null && now - timestamp < ttlMillis) {
            return true
        }

        cache.put(key, now)
        cleanExpired(now)
        return false
    }

    private fun cleanExpired(now: Long) {
        val snapshot = cache.snapshot()
        for ((key, timestamp) in snapshot) {
            if (now - timestamp >= ttlMillis) {
                cache.remove(key)
            }
        }
    }

    companion object {
        fun createKey(
            roomName: String,
            sender: String,
            message: String,
        ): String = "$roomName|$sender|$message"
    }
}
