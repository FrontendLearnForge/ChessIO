package com.example.chessio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

abstract class BaseActivity : AppCompatActivity() {
    protected lateinit var drawerLayout: DrawerLayout
    protected lateinit var navView: NavigationView
    private lateinit var currentUserRole: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        currentUserRole = sharedPreferences.getString("current_user_role", "guest") ?: "guest"
        setupToolbar()
        setupNavigationDrawer()
    }

    protected fun setupToolbar(title: String = "") {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        toolbar.findViewById<TextView>(R.id.title).text = title
        toolbar.findViewById<ImageView>(R.id.menu_icon).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    protected fun updateNavHeader() {
        val headerView = navView.getHeaderView(0)

        headerView.findViewById<ImageView>(R.id.imageView_button_back).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun setupNavigationDrawer() {
        // Получаем меню из NavigationView
        val menu = navView.menu

        // Скрываем/показываем элементы в зависимости от роли
        when (currentUserRole) {
            "Игрок" -> {
                menu.findItem(R.id.nav_create_tournament).isVisible = false
                menu.findItem(R.id.nav_users).isVisible = false
            }
            "Организатор" -> {
                menu.findItem(R.id.nav_create_tournament).isVisible = true
                menu.findItem(R.id.nav_users).isVisible = false
            }
            "Администратор" -> {
                menu.findItem(R.id.nav_create_tournament).isVisible = true
                menu.findItem(R.id.nav_users).isVisible = true
            }
            else -> { // Для "guest" или других ролей
                menu.findItem(R.id.nav_create_tournament).isVisible = false
                menu.findItem(R.id.nav_users).isVisible = false
            }
        }

        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_notifications -> startActivity(Intent(this, NotificationsActivity::class.java))
                R.id.nav_users -> {
                    startActivity(Intent(this, UsersList::class.java))
                    finish()
                }
                R.id.nav_tournaments -> {
                    startActivity(Intent(this, TournamentsList::class.java))
                    finish()
                }
                R.id.nav_create_tournament -> {
                    startActivity(Intent(this, TournamentCreate::class.java))
                    finish()
                }
                R.id.nav_info -> {
                    startActivity(Intent(this, InformationStand::class.java))
                    finish()
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                }
                R.id.nav_logout -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    // Для открытия/закрытия меню при нажатии на иконку
    override fun onSupportNavigateUp(): Boolean {
        drawerLayout.openDrawer(GravityCompat.START)
        return true
    }

}