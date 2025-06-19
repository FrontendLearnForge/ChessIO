package com.example.chessio

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationsActivity : BaseActivity() {
    private lateinit var adapter: NotificationsAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Вставляем контент турниров в content_frame из activity_menu.xml
        val notificationsContent = layoutInflater.inflate(
            R.layout.activity_notifications,
            findViewById(R.id.content_frame)
        )

        // 2. Находим tournaments_container ВНУТРИ добавленного контента
        recyclerView = notificationsContent.findViewById(R.id.notificationsRecyclerView)

        // 3. Настраиваем остальное
        setupToolbar("Уведомления")
        updateNavHeader()

//        setContentView(R.layout.activity_notifications)

//        recyclerView = findViewById(R.id.notificationsRecyclerView)
        adapter = NotificationsAdapter { notification, position ->
            // Обработка клика на уведомление
            handleNotificationClick(notification, position)
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadNotifications()
    }

    private fun handleNotificationClick(notification: Notification, position: Int) {
        // Помечаем уведомление как прочитанное локально
        adapter.markAsRead(position)

        // Отправляем запрос на сервер для отметки как прочитанного
        CoroutineScope(Dispatchers.IO).launch {
            try {
                RetrofitClient.apiService.markAsRead(notification.id)

                // Если нужно открыть турнир
                notification.tournamentId?.let { id ->
                    runOnUiThread {
                        val intent = Intent(this@NotificationsActivity, TournamentInformation::class.java)
                        intent.putExtra("tournament_id", id)
                        startActivity(intent)
                        finish()
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
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@NotificationsActivity, "Ошибка загрузки уведомлений", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}