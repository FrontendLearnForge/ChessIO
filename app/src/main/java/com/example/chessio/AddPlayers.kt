package com.example.chessio

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddPlayers : AppCompatActivity() {
    private var tournamentId: Int = 0
    private var playersList: List<Player> = emptyList()
    private var applicationsList: List<Application> = emptyList()
    private lateinit var applicationsAdapter: ApplicationsAdapter
    private var currentUserLogin: String? = null
    private var isOrganizer: Boolean = false
    private var isTeamTournament: Boolean = false

    private var deleteButtonsVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_players)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tournamentId = intent.getIntExtra("tournament_id", 0)
        currentUserLogin = getSharedPreferences("user_prefs", MODE_PRIVATE).getString("current_user_login", null)


        val buttonAddPlayer: ImageView = findViewById(R.id.imageView_add_player)
        buttonAddPlayer.setOnClickListener {
            val dialog = AddPlayerDialog(tournamentId, isTeamTournament) {
                getPlayers(tournamentId)
            }

            dialog.show(supportFragmentManager, "AddPlayerDialog")
        }

        val buttonBack: ImageView = findViewById(R.id.imageView_button_back)
        buttonBack.setOnClickListener {
            finish()
        }

        val buttonApplications: ImageView = findViewById(R.id.imageView_applications)
        buttonApplications.setOnClickListener {
            showApplicationsDialog()
        }

        val buttonDeletePlayer: ImageView = findViewById(R.id.imageView_delete_player)
        buttonDeletePlayer.setOnClickListener {
            deleteButtonsVisible = !deleteButtonsVisible
            getPlayers(tournamentId) // Перезагружаем таблицу для обновления видимости кнопок
        }
        checkIfOrganizer()

    }

    private fun checkIfOrganizer() {
        if (currentUserLogin == null) {
            Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        RetrofitClient.apiService.getTournamentById(tournamentId).enqueue(object : Callback<Tournament> {
            override fun onResponse(call: Call<Tournament>, response: Response<Tournament>) {
                if (response.isSuccessful) {
                    response.body()?.let { tournament ->
                        isOrganizer = tournament.organizerLogin == currentUserLogin
                        isTeamTournament = tournament.type.equals("Командный", ignoreCase = true)

                        findViewById<TextView>(R.id.teamHeader).visibility =
                            if (isTeamTournament) View.VISIBLE else View.GONE
                        findViewById<ImageView>(R.id.imageView_applications).visibility =
                            if (isOrganizer) View.VISIBLE else View.GONE
                        findViewById<ImageView>(R.id.imageView_add_player).visibility =
                            if (isOrganizer) View.VISIBLE else View.GONE
                        findViewById<ImageView>(R.id.imageView_delete_player).visibility =
                            if (isOrganizer) View.VISIBLE else View.GONE

                        setupRecyclerView()
                        getPlayers(tournamentId)
                    }
                }
            }

            override fun onFailure(call: Call<Tournament>, t: Throwable) {
                Toast.makeText(this@AddPlayers, "Ошибка при проверке прав", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupRecyclerView() {
        applicationsAdapter = ApplicationsAdapter(
            onStatusChanged = { application, isApproved ->
                showConfirmationDialog(application, isApproved)
            },
            isTeamTournament = isTeamTournament,
            onUserClicked = { userLogin ->
                openUserProfile(userLogin)
            }
        )

        findViewById<RecyclerView>(R.id.recycler_applications).apply {
            layoutManager = LinearLayoutManager(this@AddPlayers)
            adapter = applicationsAdapter
        }
    }

    private fun showApplicationsDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_applications_list, null)
        val dialogRecyclerView = dialogView.findViewById<RecyclerView>(R.id.recycler_applications)

        dialogRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AddPlayers)
            adapter = applicationsAdapter
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Заявки на участие")
            .setView(dialogView)
            .setNegativeButton("Закрыть") { dialog, _ -> dialog.dismiss() }
            .create()

        getPendingApplications()

        dialog.show()
    }

    private fun getPendingApplications() {
        RetrofitClient.apiService.getPendingApplicationsByTournament(tournamentId)
            .enqueue(object : Callback<List<Application>> {
                override fun onResponse(
                    call: Call<List<Application>>,
                    response: Response<List<Application>>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { applications ->
                            applicationsList = applications
                            applicationsAdapter.submitList(applications)
                        } ?: run {
                            showToast("Нет заявок на рассмотрении")
                        }
                    } else {
                        showToast("Ошибка загрузки: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<List<Application>>, t: Throwable) {
                    showToast("Ошибка сети: ${t.message ?: "Неизвестная ошибка"}")
                }
            })
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this@AddPlayers, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun openUserProfile(userLogin: String) {
        if (userLogin==null) {
            return
        }
        val intent = Intent(this, ProfileActivity::class.java).apply {
            putExtra("user_login", userLogin)
        }
        startActivity(intent)
    }


    private fun showConfirmationDialog(application: Application, isApproved: Boolean) {
        val title = if (isApproved) "Принять заявку?" else "Отклонить заявку?"
        val userInfo = "Участник: ${application.userName}"
        val teamInfo = if (isTeamTournament && !application.teamName.isNullOrEmpty()) {
            "\nКоманда: ${application.teamName}"
        } else ""
        val ratingInfo = "\nРейтинг: ${application.rate}"

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(userInfo + teamInfo + ratingInfo)
            .setPositiveButton("Да") { _, _ ->
            updateApplicationStatus(application.id, if (isApproved) "Принята" else "Отклонена")
            }
            .setNegativeButton("Нет", null)
            .show()
    }

    private fun updateApplicationStatus(applicationId: Int?, status: String) {
        if (applicationId == null) return

        val allowedStatuses = setOf("Принята", "Отклонена")
        if (status !in allowedStatuses) {
            showToast("Недопустимый статус для изменения")
            return
        }

        val application = applicationsList.find { it.id == applicationId }
        if (application == null || application.status != "На рассмотрении") {
            showToast("Заявка уже обработана или не найдена")
            return
        }

        val statusUpdate = StatusUpdateRequest(status)

        RetrofitClient.apiService.changeApplicationStatus(applicationId, statusUpdate)
            .enqueue(object : Callback<Application> {
                override fun onResponse(call: Call<Application>, response: Response<Application>) {
                    when {
                        response.isSuccessful -> {
                            applicationsList = applicationsList.filter { it.id != applicationId }
                            applicationsAdapter.submitList(applicationsList)

                            if (status == "Принята") {
                                response.body()?.let { application ->
                                    addPlayerFromApplication(application)
                                    showToast("Заявка принята, игрок добавлен")
                                }
                            } else {
                                showToast("Заявка отклонена")
                            }
                        }
                        response.code() == 404 -> {
                            showToast("Заявка не найдена на сервере")
                        }
                        else -> {
                            showToast("Ошибка сервера: ${response.message()}")
                        }
                    }
                }

                override fun onFailure(call: Call<Application>, t: Throwable) {
                    showToast("Ошибка сети: ${t.message ?: "Неизвестная ошибка"}")
                }
            })
    }


    private fun addPlayerFromApplication(application: Application) {
        val request = CreatePlayerRequest(
            userLogin = application.userLogin,
            teamName = application.teamName
        )

        RetrofitClient.apiService.addPlayer(tournamentId, request)
            .enqueue(object : Callback<Player> {
                override fun onResponse(call: Call<Player>, response: Response<Player>) {
                    if (response.isSuccessful) {
                        getPlayers(tournamentId) // Обновляем список игроков
                    } else {
                        when (response.code()) {
                            409 -> Toast.makeText(
                                this@AddPlayers,
                                "Игрок уже добавлен в турнир",
                                Toast.LENGTH_SHORT
                            ).show()
                            else -> Toast.makeText(
                                this@AddPlayers,
                                "Ошибка: ${response.message()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onFailure(call: Call<Player>, t: Throwable) {
                    Toast.makeText(
                        this@AddPlayers,
                        "Ошибка сети: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun getPlayers(currentTournamentID:Int) {
        RetrofitClient.apiService.getPlayersByTournamentId(currentTournamentID).enqueue(object : Callback<List<Player>> {
            override fun onResponse(call: Call<List<Player>>, response: Response<List<Player>>) {
                if (response.isSuccessful) {
                    response.body()?.let { players ->
                        playersList = players
                        updateTable(players)
                    }
                } else {
                    Toast.makeText(this@AddPlayers, "Ошибка: ${response.message()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<List<Player>>, t: Throwable) {
                Toast.makeText(this@AddPlayers, "Ошибка сети: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun updateTable(players: List<Player>) {
        val tableLayout: TableLayout = findViewById(R.id.tableLayout)

        tableLayout.removeViews(1, tableLayout.childCount - 1)

        val headerRow = tableLayout.getChildAt(0) as? TableRow
        val numHeader = headerRow?.getChildAt(0) as? TextView
        val nameHeader = headerRow?.getChildAt(1) as? TextView
        val rateHeader = headerRow?.getChildAt(2) as? TextView
        val teamHeader = headerRow?.getChildAt(3) as? TextView
        val buttonDeleteHeader = headerRow?.getChildAt(4) as? TextView

        teamHeader?.visibility = if (isTeamTournament) View.VISIBLE else View.GONE
        buttonDeleteHeader?.visibility = if (deleteButtonsVisible) View.VISIBLE else View.GONE

        val numWeight = 0.3f
        val nameWeight = 1f
        val rateWeight = 0.55f
        val teamWeight = if (isTeamTournament) 0.55f else 0f
        val buttonDeleteWeight = if (deleteButtonsVisible && isOrganizer) 0.4f else 0f

        numHeader?.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, numWeight)
        nameHeader?.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, nameWeight)
        rateHeader?.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, rateWeight)
        teamHeader?.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, teamWeight)
        buttonDeleteHeader?.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, buttonDeleteWeight)

        players.forEachIndexed { index, player ->
            val tableRow = TableRow(this).apply {
                layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )
            }

            // Создаем общие параметры для ячеек
            val cellLayoutParams = { weight: Float ->
                TableRow.LayoutParams(
                    0,
                    TableRow.LayoutParams.WRAP_CONTENT, // Важно: используем MATCH_PARENT для высоты
                    weight
                ).apply {
                    gravity = Gravity.CENTER_VERTICAL
                }
            }

            // Номер игрока
            val numPlayerTextView = TextView(this).apply {
                layoutParams = cellLayoutParams(numWeight)
                text = (index + 1).toString()
                background = ResourcesCompat.getDrawable(resources, R.drawable.table_cell_left, null)
                setPadding(10, 6, 10, 6)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setTextColor(ContextCompat.getColor(this@AddPlayers, R.color.black))
                ellipsize = TextUtils.TruncateAt.END
                maxLines = 1
            }

            // Имя игрока
            val nameTextView = TextView(this).apply {
                layoutParams = cellLayoutParams(nameWeight)
                text = player.fullName
                background = ResourcesCompat.getDrawable(resources, R.drawable.table_cell, null)
                setPadding(10, 6, 10, 6)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setTextColor(ContextCompat.getColor(this@AddPlayers, R.color.black))
                ellipsize = TextUtils.TruncateAt.END
                maxLines = 1
            }

            // Рейтинг игрока
            val rateTextView = TextView(this).apply {
                layoutParams = cellLayoutParams(rateWeight)
                text = player.rate.toString()
                background = ResourcesCompat.getDrawable(resources, R.drawable.table_cell, null)
                setPadding(10, 6, 10, 6)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setTextColor(ContextCompat.getColor(this@AddPlayers, R.color.black))
                ellipsize = TextUtils.TruncateAt.END
                maxLines = 1
            }

            val deleteButton = ImageView(this, null, 0, R.style.DeleteImageButtonStyle).apply {
                layoutParams = cellLayoutParams(buttonDeleteWeight)
                background = ResourcesCompat.getDrawable(resources, R.drawable.table_cell, null)
                setPadding(10, 6, 10, 6)
                visibility = if (deleteButtonsVisible && isOrganizer) View.VISIBLE else View.GONE

                // Уменьшаем размер изображения
                adjustViewBounds = true
                maxHeight = 26.dpToPx() // Конвертируем dp в пиксели
                maxWidth = 26.dpToPx()

                setOnClickListener {
                    showDeleteConfirmationDialog(player)
                }
            }

            tableRow.addView(numPlayerTextView)
            tableRow.addView(nameTextView)
            tableRow.addView(rateTextView)

            if (isTeamTournament) {
                val teamTextView = TextView(this).apply {
                    layoutParams = cellLayoutParams(teamWeight)
                    text = player.teamName ?: "-"
                    background = ResourcesCompat.getDrawable(resources, R.drawable.table_cell, null)
                    setPadding(10, 6, 10, 6)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                    setTextColor(ContextCompat.getColor(this@AddPlayers, R.color.black))
                    ellipsize = TextUtils.TruncateAt.END
                    maxLines = 1
                }
                tableRow.addView(teamTextView)
            }
            tableRow.addView(deleteButton)
            tableLayout.addView(tableRow)
        }
    }

    // Extension функция для конвертации dp в px
    private fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    private fun showDeleteConfirmationDialog(player: Player) {
        AlertDialog.Builder(this)
            .setTitle("Удалить игрока ?")
            .setMessage("Вы уверены, что хотите удалить ${player.fullName} из турнира?")
            .setPositiveButton("Да") { _, _ ->
                deletePlayer(player)
            }
            .setNegativeButton("Нет", null)
            .show()
    }

    private fun deletePlayer(player: Player) {
        if (currentUserLogin == null) {
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.apiService.deletePlayer(tournamentId, player.id ?: 0, currentUserLogin!!)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    when {
                        response.isSuccessful -> {
                            Toast.makeText(this@AddPlayers, "Игрок удален", Toast.LENGTH_SHORT).show()
                            getPlayers(tournamentId)
                        }
                        response.code() == 403 -> {
                            Toast.makeText(
                                this@AddPlayers,
                                "Недостаточно прав для удаления",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        response.code() == 404 -> {
                            Toast.makeText(
                                this@AddPlayers,
                                "Игрок не найден",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> {
                            Toast.makeText(
                                this@AddPlayers,
                                "Ошибка: ${response.message()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(
                        this@AddPlayers,
                        "Ошибка сети: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

}
