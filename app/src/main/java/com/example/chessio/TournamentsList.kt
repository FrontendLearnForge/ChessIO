package com.example.chessio

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class TournamentsList : BaseActivity() {

    private lateinit var userLogin: String
    private lateinit var userRole: String
    private lateinit var tournamentsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tournamentsContent = layoutInflater.inflate(
            R.layout.activity_tournaments_list,
            findViewById(R.id.content_frame)
        )
        tournamentsContainer = tournamentsContent.findViewById(R.id.tournaments_container)
        setupToolbar("Список турниров")
        updateNavHeader()
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        userLogin = sharedPreferences.getString("current_user_login", null) ?: run {
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        userRole = sharedPreferences.getString("current_user_role", "guest") ?: "guest"

        loadTournaments()
    }


    private fun loadTournaments() {
        RetrofitClient.apiService.getTournaments().enqueue(object : Callback<List<Tournament>> {
            override fun onResponse(call: Call<List<Tournament>>, response: Response<List<Tournament>>) {
                if (response.isSuccessful) {
                    response.body()?.let { tournaments ->
                        if (tournaments.isEmpty()) {
                            showEmptyState()
                        } else {
                            displayTournaments(tournaments)
                        }
                    }
                } else {
                    Toast.makeText(
                        this@TournamentsList,
                        "Ошибка при загрузке турниров: ${response.message()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<Tournament>>, t: Throwable) {
                Toast.makeText(
                    this@TournamentsList,
                    "Ошибка сети: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun displayTournaments(tournaments: List<Tournament>) {
        tournamentsContainer.removeAllViews()

        val serverDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        serverDateFormat.timeZone = TimeZone.getTimeZone("UTC")

        val displayDateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        displayDateTimeFormat.timeZone = TimeZone.getTimeZone("Europe/Moscow")

        try {
            val sortedTournaments = tournaments.sortedByDescending {
                serverDateFormat.parse(it.dateStart)?.time ?: 0
            }

            sortedTournaments.forEachIndexed { index, tournament ->
                val itemView = LayoutInflater.from(this)
                    .inflate(R.layout.item_tournament, tournamentsContainer, false)

                val startDate = serverDateFormat.parse(tournament.dateStart)
                val endDate = serverDateFormat.parse(tournament.dateEnd)

                val displayStart = startDate?.let { displayDateTimeFormat.format(it) } ?: tournament.dateStart
                val displayEnd = endDate?.let { displayDateTimeFormat.format(it) } ?: tournament.dateEnd

                itemView.findViewById<TextView>(R.id.tournament_name).text = tournament.name
                itemView.findViewById<TextView>(R.id.tournament_date_start).text =
                    "Начало: $displayStart"
                itemView.findViewById<TextView>(R.id.tournament_date_end).text =
                    "Окончание: $displayEnd"
                itemView.findViewById<TextView>(R.id.tournament_address).text =
                    "Адрес: ${tournament.address}"
                itemView.findViewById<TextView>(R.id.tournament_type).text =
                    "Тип: ${tournament.type}"
                itemView.findViewById<TextView>(R.id.tournament_format).text =
                    "Формат: ${tournament.format}"
                itemView.findViewById<TextView>(R.id.tournament_status).text =
                    "Статус турнира: ${tournament.status}"

                val deleteButton = itemView.findViewById<ImageButton>(R.id.button_delete)

                val showDeleteButton = userRole == "Администратор" ||
                        (userRole == "Организатор" && tournament.organizerLogin == userLogin)

                deleteButton.visibility = if (showDeleteButton) View.VISIBLE else View.GONE

                deleteButton.setOnClickListener {
                    tournament.id?.let { tournamentId ->
                        showDeleteConfirmationDialog(tournamentId, tournament.name)
                    }
                }

                itemView.setOnClickListener {
                    tournament.id?.let { tournamentId ->
                        startActivity(Intent(this, TournamentInformation::class.java).apply {
                            putExtra("tournament_id", tournamentId)
                        })
                    }
                }

                tournamentsContainer.addView(itemView, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = if (index == 0) 16 else 8
                    setMargins(4.dpToPx(), topMargin.dpToPx(), 4.dpToPx(), 4.dpToPx())
                })
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка загрузки турниров", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    private fun showEmptyState() {
        tournamentsContainer.removeAllViews()

        val itemView = LayoutInflater.from(this)
            .inflate(R.layout.item_tournament, tournamentsContainer, false).apply {
                findViewById<TextView>(R.id.tournament_name).text = "Нет доступных турниров"
                findViewById<TextView>(R.id.tournament_date_start).text = "Нажмите, чтобы создать новый"
                findViewById<TextView>(R.id.tournament_date_end).text = ""
                findViewById<TextView>(R.id.tournament_address).text = ""
                findViewById<TextView>(R.id.tournament_type).text = ""
                findViewById<TextView>(R.id.tournament_format).text = ""

                setOnClickListener {
                    startActivity(Intent(context, TournamentCreate::class.java).apply {})
                    finish()
                }
            }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }

        tournamentsContainer.apply {
            gravity = Gravity.CENTER
            addView(itemView, params)
        }
    }

    private fun showDeleteConfirmationDialog(tournamentId: Int, tournamentName: String) {
        AlertDialog.Builder(this)
            .setTitle("Удаление турнира")
            .setMessage("Вы уверены, что хотите удалить турнир: ${tournamentName}?")
            .setPositiveButton("Да") { _, _ ->
                deleteTournament(tournamentId, userLogin)
            }
            .setNegativeButton("Нет", null)
            .create()
            .show()
    }

    private fun deleteTournament(tournamentId: Int, userLogin: String) {
        RetrofitClient.apiService.deleteTournament(tournamentId, userLogin).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@TournamentsList,
                        "Турнир успешно удален",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadTournaments()
                } else {
                    Toast.makeText(
                        this@TournamentsList,
                        "Ошибка при удалении: ${response.message()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(
                    this@TournamentsList,
                    "Ошибка сети: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}