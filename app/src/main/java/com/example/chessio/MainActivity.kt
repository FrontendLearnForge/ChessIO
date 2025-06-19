package com.example.chessio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var webSocketManager: WebSocketManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val login: EditText = findViewById(R.id.login)
        val password: EditText = findViewById(R.id.password)
        val buttonEnter: Button = findViewById(R.id.button_enter)
        val buttonRegister: Button = findViewById(R.id.button_register)

        buttonRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        buttonEnter.setOnClickListener {
            val loginText = login.text.toString().trim()
            val passwordText = password.text.toString().trim()

            if (loginText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(this, "Введите логин и пароль", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            enterUser(EnterUser(loginText, passwordText))
        }
    }

    private fun enterUser(user: EnterUser) {
        RetrofitClient.apiService.enterUser(user).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val currentUser = response.body()
                    if (currentUser != null) {
                        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                        sharedPref.edit().apply {
                            putString("current_user_login", currentUser.login)
                            putString("current_user_role", currentUser.role)
                            apply()
                        }

                        initWebSocket(currentUser.login)

                        Toast.makeText(this@MainActivity, "Вход выполнен", Toast.LENGTH_SHORT)
                            .show()

                        val intent = Intent(this@MainActivity, TournamentsList::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Ошибка: пустой ответ сервера",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    when (response.code()) {
                        401 -> Toast.makeText(
                            this@MainActivity,
                            "Неверный логин или пароль",
                            Toast.LENGTH_SHORT
                        ).show()

                        else -> Toast.makeText(
                            this@MainActivity,
                            "Ошибка ${response.code()}: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Toast.makeText(
                    this@MainActivity,
                    "Сетевая ошибка: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun initWebSocket(userLogin: String) {
        webSocketManager = WebSocketManager(this, userLogin)
        webSocketManager.connect()
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        if (::webSocketManager.isInitialized) {
//            webSocketManager.disconnect()
//        }
//    }
}