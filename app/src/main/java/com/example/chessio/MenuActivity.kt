package com.example.chessio
//
//import android.content.Intent
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.widget.Button
//import android.widget.EditText
//import android.widget.ImageView
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.GravityCompat
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import androidx.drawerlayout.widget.DrawerLayout
//import androidx.navigation.findNavController
//import androidx.navigation.ui.AppBarConfiguration
//import androidx.navigation.ui.setupActionBarWithNavController
//import androidx.navigation.ui.setupWithNavController
//import com.google.android.material.navigation.NavigationView
//
//class MainActivity : AppCompatActivity() {
//    private lateinit var appBarConfiguration: AppBarConfiguration
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
//        val navView: NavigationView = findViewById(R.id.nav_view)
//        val navController = findNavController(R.id.nav_host_fragment)
//
//        // Настройка верхних пунктов меню (если нужно)
//        appBarConfiguration = AppBarConfiguration(
//            setOf(R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow),
//            drawerLayout
//        )
//
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        navView.setupWithNavController(navController)
//
//        // Обработка кликов по пунктам меню
//        navView.setNavigationItemSelectedListener { menuItem ->
//            when (menuItem.itemId) {
//                R.id.nav_users -> {
//                    startActivity(Intent(this, UsersList::class.java))
//                }
//                R.id.nav_tournaments -> {
//                    startActivity(Intent(this, TournamentsList::class.java))
//                }
//                R.id.nav_create_tournament -> {
//                    startActivity(Intent(this, TournamentCreate::class.java))
//                }
//                R.id.nav_info -> {
//                    startActivity(Intent(this, InformationStand::class.java))
//                }
//                R.id.nav_profile -> {
//                    startActivity(Intent(this, ProfileActivity::class.java))
//                }
//                R.id.nav_logout -> {
//                    startActivity(Intent(this, MainActivity::class.java))
//                    finish()
//                }
//            }
//            drawerLayout.closeDrawer(GravityCompat.START)
//            true
//        }
//    }
//
//    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.nav_host_fragment)
//        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
//    }
//}