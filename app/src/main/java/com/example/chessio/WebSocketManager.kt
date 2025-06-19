package com.example.chessio

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class WebSocketManager(
    private val context: Context,
    private val userLogin: String
) {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS) // Keep-alive
        .build()

    fun connect() {
        val request = Request.Builder()
            .url("ws://192.168.0.103:5000")
            .addHeader("User-Login", userLogin)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "Connected")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Received: $text")
                handleMessage(text)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "Closed: $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Error: ${t.message}")
                // Переподключение через 5 секунд
                android.os.Handler().postDelayed({ connect() }, 5000)
            }
        })
    }

    private fun handleMessage(message: String) {
        try {
            val data = Gson().fromJson(message, Map::class.java)
            when (data["type"]) {
                "new-notification" -> {
                    val notificationJson = Gson().toJson(data["data"])
                    val notification = Gson().fromJson(notificationJson, Notification::class.java)

                    // Проверяем, что уведомление для текущего пользователя
                    if (notification.userLogin == userLogin) {
                        NotificationHandler.handleNewNotification(context, notification)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("WebSocket", "Error parsing message", e)
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
    }
}