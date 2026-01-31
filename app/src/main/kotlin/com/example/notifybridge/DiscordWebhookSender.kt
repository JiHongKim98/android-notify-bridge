package com.example.notifybridge

import android.util.Log
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

object DiscordWebhookSender {
    private const val TAG = "DiscordWebhook"
    private val executor = Executors.newSingleThreadExecutor()

    fun send(
        webhookUrl: String,
        roomName: String,
        sender: String,
        message: String,
    ) {
        executor.execute {
            try {
                val content = "**[$roomName] $sender**\n$message"
                val payload =
                    JSONObject().apply {
                        put("content", content)
                    }

                val url = URL(webhookUrl)
                val connection = url.openConnection() as HttpURLConnection

                connection.apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    doOutput = true
                    connectTimeout = 5000
                    readTimeout = 5000
                }

                OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(payload.toString())
                    writer.flush()
                }

                val responseCode = connection.responseCode
                if (responseCode in 200..299) {
                    Log.d(TAG, "Message sent successfully")
                } else {
                    Log.e(TAG, "Failed to send message: HTTP $responseCode")
                }

                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error sending webhook: ${e.message}")
            }
        }
    }
}
