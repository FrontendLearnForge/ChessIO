package com.example.chessio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationsActivity : BaseActivity() {
    private lateinit var adapter: NotificationsAdapter
    private lateinit var recyclerView: RecyclerView

    // Receiver для получения новых уведомлений через Broadcast
    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra("notification", Notification::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra("notification")
            }

            notification?.let {
                // Добавляем новое уведомление в начало списка
                val newList = mutableListOf<Notification>().apply {
                    add(it)
                    addAll(adapter.currentList)
                }
                adapter.submitList(newList)
                recyclerView.smoothScrollToPosition(0)

                // Обновляем бейдж уведомлений в навигации
                updateNotificationBadge(newList.count { notification -> !notification.isRead })
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val notificationsContent = layoutInflater.inflate(
            R.layout.activity_notifications,
            findViewById(R.id.content_frame)
        )
        recyclerView = notificationsContent.findViewById(R.id.notificationsRecyclerView)
        setupToolbar("Уведомления")
        updateNavHeader()

        // Инициализация адаптера
        adapter = NotificationsAdapter { notification, position ->
            handleNotificationClick(notification, position)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Регистрация BroadcastReceiver для новых уведомлений
        LocalBroadcastManager.getInstance(this).registerReceiver(
            notificationReceiver,
            IntentFilter("NEW_NOTIFICATION")
        )

        // Загрузка уведомлений
        loadNotifications()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Отмена регистрации Receiver при уничтожении активности
        LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationReceiver)
    }

    private fun handleNotificationClick(notification: Notification, position: Int) {
        // Помечаем уведомление как прочитанное локально
        adapter.markAsRead(position)

        // Отправляем запрос на сервер для отметки как прочитанного
        CoroutineScope(Dispatchers.IO).launch {
            try {
                RetrofitClient.apiService.markAsRead(notification.id)

                // Обновляем бейдж после прочтения
                val unreadCount = adapter.currentList.count { !it.isRead }
                runOnUiThread {
                    updateNotificationBadge(unreadCount)
                }

                // Если нужно открыть турнир
                notification.tournamentId?.let { id ->
                    runOnUiThread {
                        val intent = Intent(this@NotificationsActivity, TournamentInformation::class.java)
                        intent.putExtra("tournament_id", id)
                        startActivity(intent)
                        // Убрали finish() чтобы оставаться в активности
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    // В случае ошибки возвращаем исходный вид
                    adapter.notifyItemChanged(position)
                    Toast.makeText(
                        this@NotificationsActivity,
                        "Ошибка: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun loadNotifications() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userLogin = sharedPreferences.getString("current_user_login", null) ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val notifications = RetrofitClient.apiService.getNotifications(userLogin)
                runOnUiThread {
                    adapter.submitList(notifications)

                    // Обновляем бейдж при загрузке
                    updateNotificationBadge(notifications.count { !it.isRead })
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@NotificationsActivity,
                        "Ошибка загрузки уведомлений",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun updateNotificationBadge(unreadCount: Int) {
        // Реализуйте обновление бейджа в навигации
        // navView.getOrCreateBadge(R.id.nav_notifications).number = unreadCount

        // Если используете кастомное решение:
        // badgeView.text = if (unreadCount > 0) unreadCount.toString() else ""
    }
}