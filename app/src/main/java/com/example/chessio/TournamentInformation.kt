package com.example.chessio

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class TournamentInformation : AppCompatActivity() {
    private var tournamentId: Int? = null
    private lateinit var tournamentType: String
    private lateinit var tournamentFormat: String
    private var tournamentNumberTours: Int=0

    private lateinit var buttonBack: ImageView
    private lateinit var buttonListPlayers: ImageView
    private lateinit var buttonTours: ImageView
    private lateinit var buttonApplication: Button

    private lateinit var textListPlayers: TextView
    private lateinit var textTours: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tournament_information)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tournamentId = intent.getIntExtra("tournament_id", -1)
        if (tournamentId != -1) {
            loadTournamentData(tournamentId!!)
        } else {
            Toast.makeText(this, "Ошибка: ID турнира не найден", Toast.LENGTH_SHORT).show()
        }

        buttonBack = findViewById(R.id.imageView_button_back)
        buttonBack.setOnClickListener {
            val intent = Intent(this, TournamentsList::class.java)
            startActivity(intent)
            finish()
        }

        buttonListPlayers = findViewById(R.id.listPlayers_button)
        buttonListPlayers.setOnClickListener {
            tournamentId?.let { tournamentId ->
                startActivity(Intent(this, AddPlayers::class.java).apply {
                    putExtra("tournament_id", tournamentId)
                })
            }
        }

        textListPlayers=findViewById(R.id.listPlayers_textView)
        textListPlayers.setOnClickListener {
            tournamentId?.let { tournamentId ->
                startActivity(Intent(this, AddPlayers::class.java).apply {
                    putExtra("tournament_id", tournamentId)
                })
            }
        }


        buttonTours = findViewById(R.id.tours_button)
        buttonTours.setOnClickListener {
            tournamentId?.let { tournamentId ->
                startActivity(Intent(this, Tours::class.java).apply {
                    putExtra("tournament_id", tournamentId)
                    putExtra("tournament_type", tournamentType)
                    putExtra("tournament_format", tournamentFormat)
                    putExtra("tournament_numberTours", tournamentNumberTours)
                })
            }
        }

        textTours=findViewById(R.id.tours_textView)
        textTours.setOnClickListener {
            tournamentId?.let { tournamentId ->
                startActivity(Intent(this, Tours::class.java).apply {
                    putExtra("tournament_id", tournamentId)
                    putExtra("tournament_type", tournamentType)
                    putExtra("tournament_format", tournamentFormat)
                    putExtra("tournament_numberTours", tournamentNumberTours)
                })
            }
        }

        buttonApplication = findViewById(R.id.application_button)
        buttonApplication.setOnClickListener {
            showTeamDialog()
        }
    }

    private fun loadTournamentData(id: Int) {
        RetrofitClient.apiService.getTournamentById(id).enqueue(object : Callback<Tournament> {
            override fun onResponse(call: Call<Tournament>, response: Response<Tournament>) {
                if (response.isSuccessful) {
                    response.body()?.let { tournament ->
                        displayTournamentData(tournament)
                    }
                } else {
                    Toast.makeText(this@TournamentInformation, "Ошибка при загрузке турнира: ${response.message()}", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<Tournament>, t: Throwable) {
                Toast.makeText(this@TournamentInformation, "Ошибка сети: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun displayTournamentData(tournament: Tournament) {
        tournamentType=tournament.type
        tournamentFormat=tournament.format
        tournamentNumberTours=tournament.numberTours
        // Формат для парсинга даты с сервера (ISO 8601)
        val serverDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        serverDateFormat.timeZone = TimeZone.getTimeZone("UTC")

        // Форматы для отображения
        val displayDateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        displayDateTimeFormat.timeZone = TimeZone.getTimeZone("Europe/Moscow")

        val startDate = serverDateFormat.parse(tournament.dateStart)
        val endDate = serverDateFormat.parse(tournament.dateEnd)

        val displayStart = startDate?.let { displayDateTimeFormat.format(it) } ?: tournament.dateStart
        val displayEnd = endDate?.let { displayDateTimeFormat.format(it) } ?: tournament.dateEnd

        findViewById<TextView>(R.id.tournamentName).text = tournament.name
        findViewById<TextView>(R.id.tournamentDateStart).text = displayStart
        findViewById<TextView>(R.id.tournamentDateEnd).text = displayEnd
        findViewById<TextView>(R.id.tournamentNameReferee).text = tournament.nameReferee
        findViewById<TextView>(R.id.tournamentAddress).text = tournament.address
        findViewById<TextView>(R.id.numberTours).text = tournament.numberTours.toString()
        findViewById<TextView>(R.id.tournamentType).text = tournament.type
        findViewById<TextView>(R.id.tournamentFormat).text = tournament.format
    }

    private fun showTeamDialog() {
        val tournamentType = findViewById<TextView>(R.id.tournamentType).text.toString()

        if (tournamentType.equals("Командный", ignoreCase = true)) {
            // Диалог для командного турнира
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_team_name, null)
            val editTeamName: EditText = dialogView.findViewById(R.id.edit_team_name)

            val dialogBuilder = AlertDialog.Builder(this)
                .setTitle("Заявление на участие")
                .setMessage("Для подачи заявления на участие введите название вашей команды:")
                .setView(dialogView)
                .setPositiveButton("OK") { dialog, which ->
                    val teamName = editTeamName.text.toString()
                    submitApplication(teamName)
                }
                .setNegativeButton("Отмена") { dialog, which ->
                    dialog.dismiss()
                }

            dialogBuilder.create().show()
        } else {
            // Диалог для личного турнира
            AlertDialog.Builder(this)
                .setTitle("Заявление на участие")
                .setMessage("Подать заявление на участие в турнире?")
                .setPositiveButton("OK") { dialog, which ->
                    submitApplication(null)
                }
                .setNegativeButton("Отмена") { dialog, which ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }
    }

    private fun submitApplication(teamName: String?) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userLogin = sharedPreferences.getString("current_user_login", null)

        if (userLogin == null) {
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show()
            return
        }

        createNewApplication(userLogin, teamName)
    }

    private fun createNewApplication(userLogin: String, teamName: String?) {
        tournamentId?.let { id ->
            val request = CreateApplicationRequest(userLogin, teamName)

            RetrofitClient.apiService.createApplication(id, request)
                .enqueue(object : Callback<Application> {
                    override fun onResponse(call: Call<Application>, response: Response<Application>) {
                        when {
                            response.isSuccessful -> {
                                val message = if (teamName != null) {
                                    "Заявка команды $teamName отправлена"
                                } else {
                                    "Ваша заявка на участие отправлена"
                                }
                                Toast.makeText(this@TournamentInformation, message, Toast.LENGTH_SHORT).show()
                            }
                            response.code() == 409 -> {
                                showApplicationExistsDialog(response.errorBody()?.string())
                            }
                            else -> {
                                Toast.makeText(
                                    this@TournamentInformation,
                                    "Ошибка: ${response.message()}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    override fun onFailure(call: Call<Application>, t: Throwable) {
                        Toast.makeText(
                            this@TournamentInformation,
                            "Ошибка сети: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }


    private fun showApplicationExistsDialog(errorMessage: String?) {
        val message = when {
            errorMessage?.contains("участвуете") == true -> "Вы уже участвуете в этом турнире"
            errorMessage?.contains("заявка") == true -> "У вас уже есть заявка на этот турнир (ожидает рассмотрения)"
            else -> "Невозможно создать заявку"
        }

        AlertDialog.Builder(this)
            .setTitle("Внимание")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}