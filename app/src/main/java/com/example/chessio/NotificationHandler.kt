package com.example.chessio

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

object NotificationHandler {
    private const val CHANNEL_ID = "chess_notifications"

    fun handleNewNotification(context: Context, notification: Notification) {
        // 1. Отправляем broadcast для обновления UI
        val intent = Intent("NEW_NOTIFICATION")
        intent.putExtra("notification", notification)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)

        // 2. Показываем системное уведомление
        showSystemNotification(context, notification)
    }

    private fun showSystemNotification(context: Context, notification: Notification) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Создаем канал для Android 8.0+
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Chess Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Уведомления о турнирах"
        }
        notificationManager.createNotificationChannel(channel)

        // Intent для открытия приложения при нажатии на уведомление
        val intent = Intent(context, NotificationsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Создаем уведомление
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_app) // Добавьте свою иконку
            .setContentTitle("Новое уведомление")
            .setContentText(notification.message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(notification.id, builder.build())
    }
}