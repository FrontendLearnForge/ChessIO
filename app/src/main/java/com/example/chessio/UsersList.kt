package com.example.chessio

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class UsersList : BaseActivity() {

    private lateinit var userLogin: String
    private lateinit var userRole: String
    private lateinit var usersContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val usersContent = layoutInflater.inflate(
            R.layout.activity_users_list,
            findViewById(R.id.content_frame)
        )

        usersContainer = usersContent.findViewById(R.id.users_container)

        setupToolbar("Список пользователей")
        updateNavHeader()

        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        userLogin = sharedPreferences.getString("current_user_login", null) ?: run {
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        userRole = sharedPreferences.getString("current_user_role", "guest") ?: "guest"

        loadUsers()
    }

    private fun loadUsers() {
        RetrofitClient.apiService.getUsers().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    response.body()?.let { users ->
                            displayUsers(users)
                    }
                } else {
                    Toast.makeText(
                        this@UsersList,
                        "Ошибка при загрузке пользователей: ${response.message()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Toast.makeText(
                    this@UsersList,
                    "Ошибка сети: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun displayUsers(users: List<User>) {
        usersContainer.removeAllViews()

        try {
            val sortedUsers = users.sortedBy { it.login.lowercase() }

            sortedUsers.forEachIndexed { index, user ->
                val itemView = LayoutInflater.from(this)
                    .inflate(R.layout.item_user, usersContainer, false)

                itemView.findViewById<TextView>(R.id.user_login).text = "Пользователь: ${user.login}"
                itemView.findViewById<TextView>(R.id.user_name).text = "ФИО: ${user.username}"
                itemView.findViewById<TextView>(R.id.user_rate).text = "Рейтинг: ${user.rate}"
                itemView.findViewById<TextView>(R.id.user_role).text = "Роль: ${user.role}"

                val deleteButton = itemView.findViewById<ImageButton>(R.id.button_delete)

                deleteButton.visibility = if (userRole == "Администратор") View.VISIBLE else View.GONE

                deleteButton.setOnClickListener {
                    showDeleteConfirmationDialog(user.login)
                }

                itemView.setOnClickListener {
                    user.login.let { login ->
                        startActivity(Intent(this, ProfileActivity::class.java).apply {
                            putExtra("user_login", login)
                        })
                    }
                }

                usersContainer.addView(itemView, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = if (index == 0) 16 else 8
                    setMargins(4.dpToPx(), topMargin.dpToPx(), 4.dpToPx(), 4.dpToPx())
                })
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка загрузки пользователей", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    private fun showDeleteConfirmationDialog(login: String) {
        AlertDialog.Builder(this)
            .setTitle("Удаление пользователя")
            .setMessage("Вы уверены, что хотите удалить пользователя: ${login}?")
            .setPositiveButton("Да") { _, _ ->
                deleteUser(login, userLogin)
            }
            .setNegativeButton("Нет", null)
            .create()
            .show()
    }

    private fun deleteUser(login: String, userLogin: String) {
        RetrofitClient.apiService.deleteUser(login, userLogin).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@UsersList,
                        "Пользователь успешно удален",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadUsers()
                } else {
                    Toast.makeText(
                        this@UsersList,
                        "Ошибка при удалении: ${response.message()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(
                    this@UsersList,
                    "Ошибка сети: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}