package com.example.chessio

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Calendar
import android.widget.TextView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class TournamentCreate : BaseActivity() {

    private lateinit var tournamentDateStart: TextView
    private lateinit var tournamentDateEnd: TextView
    private lateinit var tournamentName: EditText
    private lateinit var tournamentRefereeName: EditText
    private lateinit var tournamentAddress: EditText
    private lateinit var tournamentNumberTours: EditText
    private lateinit var tournamentType: Spinner
    private lateinit var tournamentFormat: Spinner

    private val selectedStartDateTime = Calendar.getInstance()
    private val selectedEndDateTime = Calendar.getInstance()

    private lateinit var startDateTime: String
    private lateinit var endDateTime: String

    private var type: String = ""
    private var format: String = ""

    private lateinit var tournamentCreateContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_tournament_create)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }


        // 1. Вставляем контент турниров в content_frame из activity_menu.xml
        val tournamentCreateContent = layoutInflater.inflate(
            R.layout.activity_tournament_create,
            findViewById(R.id.content_frame)
        )

        // 2. Находим tournament_container ВНУТРИ добавленного контента
        tournamentCreateContainer = tournamentCreateContent.findViewById(R.id.tournamnet_create_container)

        // 3. Настраиваем остальное
        setupToolbar("Создание турнира")
        updateNavHeader()


        tournamentDateStart = findViewById(R.id.date_start_tournam)
        tournamentDateEnd = findViewById(R.id.date_end_tournam)
        tournamentName = findViewById(R.id.name_tournam)
        tournamentRefereeName = findViewById(R.id.name_referee_tournam)
        tournamentAddress = findViewById(R.id.address_tournam)
        tournamentNumberTours = findViewById(R.id.number_tours)
        tournamentType = findViewById(R.id.type_tournam)
        tournamentFormat = findViewById(R.id.format_tournam)


        val buttonCreateTournament: Button =findViewById(R.id.button_create_tournam)

        setupSpinners()

        setupDateTimePickers()

        buttonCreateTournament.setOnClickListener {
                val name = tournamentName.text.toString().trim()
                val nameReferee = tournamentRefereeName.text.toString().trim()
                val address = tournamentAddress.text.toString().trim()
                val numberToursText = tournamentNumberTours.text.toString().trim()
                // Получаем логин текущего пользователя из SharedPreferences
                val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
                val userLogin = sharedPreferences.getString("current_user_login", null) ?: run {
                    Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                if (!::startDateTime.isInitialized || !::endDateTime.isInitialized ||
                    name.isEmpty() || nameReferee.isEmpty() || numberToursText.isEmpty() ||
                    type.isEmpty() || format.isEmpty()) {
                    Toast.makeText(this, "Не все поля заполнены", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                if (selectedEndDateTime.timeInMillis <= selectedStartDateTime.timeInMillis) {
                    Toast.makeText(this, "Дата окончания должна быть позже даты начала", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }


                val numberTours = try {
                    numberToursText.toInt()
                } catch (e: NumberFormatException) {
                    0
                }


                val tournament = Tournament(dateStart = startDateTime, dateEnd = endDateTime,
                    name=name, nameReferee=nameReferee, address = address, numberTours=numberTours,
                    type=type, format=format, organizerLogin = userLogin, status = "Создан")
                RetrofitClient.apiService.createTournament(tournament).enqueue(object : Callback<Tournament> {
                    override fun onResponse(call: Call<Tournament>, response: Response<Tournament>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@TournamentCreate, "Турнир $name создан", Toast.LENGTH_LONG).show()
                            val createdTournament = response.body()
                            val tournamentId = createdTournament?.id
                            val intent = Intent(this@TournamentCreate, TournamentInformation::class.java)
                            if (tournamentId != null) {
                                intent.putExtra("tournament_id", tournamentId.toInt())
                            }
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this@TournamentCreate,
                                "Ошибка: ${response.message()}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<Tournament>, t: Throwable) {
                        Toast.makeText(this@TournamentCreate, "Ошибка сети: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
        }

    }

    private fun setupSpinners() {
        val tournamentTypes = arrayOf("Личный", "Командный")
        val tournamentFormats = arrayOf("Швейцарская система", "Круговая система", "Олимпийская система")
        // Для типа турнира
        tournamentType.adapter = ArrayAdapter(this, R.layout.spinner_item, tournamentTypes).apply {
            setDropDownViewResource(R.layout.spinner_dropdown_item) // Ваш выпадающий макет
        }

        tournamentType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                type = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                type = ""
            }
        }

        tournamentFormat.adapter = ArrayAdapter(this, R.layout.spinner_item, tournamentFormats).apply {
            setDropDownViewResource(R.layout.spinner_dropdown_item)
        }

        tournamentFormat.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                format = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                format = ""
            }
        }
    }

    private fun setupDateTimePickers() {
        tournamentDateStart.setOnClickListener { showDateTimePicker(true) }
        tournamentDateEnd.setOnClickListener { showDateTimePicker(false) }
    }

    private fun showDateTimePicker(isStartDate: Boolean) {
        val moscowTimeZone = TimeZone.getTimeZone("Europe/Moscow")

        val currentCalendar = if (isStartDate) {
            selectedStartDateTime.apply { timeZone = moscowTimeZone }
        } else {
            selectedEndDateTime.apply { timeZone = moscowTimeZone }
        }

        DatePickerDialog(
            this,
            { _, year, month, day ->
                currentCalendar.set(year, month, day)

                TimePickerDialog(
                    this,
                    { _, hour, minute ->
                        currentCalendar.set(Calendar.HOUR_OF_DAY, hour)
                        currentCalendar.set(Calendar.MINUTE, minute)

                        // Формат для отображения пользователю
                        val displayFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).apply {
                            timeZone = moscowTimeZone
                        }

                        // Формат для отправки на сервер
                        val serverFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply {
                            timeZone = moscowTimeZone
                        }

                        if (isStartDate) {
                            startDateTime = serverFormat.format(currentCalendar.time)
                            tournamentDateStart.text = displayFormat.format(currentCalendar.time)
                        } else {
                            endDateTime = serverFormat.format(currentCalendar.time)
                            tournamentDateEnd.text = displayFormat.format(currentCalendar.time)
                        }
                    },
                    currentCalendar.get(Calendar.HOUR_OF_DAY),
                    currentCalendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            currentCalendar.get(Calendar.YEAR),
            currentCalendar.get(Calendar.MONTH),
            currentCalendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

}