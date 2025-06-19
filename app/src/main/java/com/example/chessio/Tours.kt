package com.example.chessio

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class Tours : AppCompatActivity() {
    private var tournamentId: Int = 0
    private lateinit var tournamentType: String
    private lateinit var tournamentFormat: String

    private var countTour: Int = 0
    private var maxPossibleCountTour: Int = 0
    private var isStatistic: Boolean = false

    private lateinit var initialPlayers: List<Player>
    private lateinit var currentPlayers: MutableList<Player>
    private val toursHistory = mutableListOf<List<Game>>()
    private val playersHistory = mutableListOf<List<Player>>()

    // UI элементы
    private lateinit var listPlayersLabel: TextView
    private lateinit var tournamentCountTours: TextView
    private lateinit var buttonStatistic: ImageView
    private lateinit var buttonExport: ImageView
    private lateinit var buttonLeft: ImageButton
    private lateinit var buttonRight: ImageButton
    private lateinit var buttonChange: Button
    private lateinit var buttonBack: ImageView


    private lateinit var scrollPairs: ScrollView
    private lateinit var scrollStatistics: ScrollView

    private var bergerTable: List<List<Pair<Int, Int>>> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tours)

        // Инициализация UI элементов
        tournamentId = intent.getIntExtra("tournament_id", 0)
        tournamentType = intent.getStringExtra("tournament_type")!!
        tournamentFormat = intent.getStringExtra("tournament_format")!!
        listPlayersLabel= findViewById(R.id.listPlayers_label)
        tournamentCountTours = findViewById(R.id.CountTours)
        tournamentCountTours.text = countTour.toString()
        buttonStatistic = findViewById(R.id.button_statistic)
        buttonExport = findViewById(R.id.button_export)
        buttonBack = findViewById(R.id.imageView_button_back)
        buttonLeft = findViewById(R.id.button_left)
        buttonRight = findViewById(R.id.button_right)

        scrollPairs = findViewById(R.id.scrollView2)
        scrollStatistics = findViewById(R.id.scrollViewStatistic)

//        getPlayers(tournamentId) // записали в playersList


        val playersData = listOf(
            "Романов Роман" to 1000,
            "Вадимов Вадим" to 1000,
            "Олегов Олег" to 1000,
            "Данилов Данил" to 1000,
            "Егоров Егор" to 1000,
            "Максимов Максим" to 1000
        )

        // Генерация начальных данных
        initialPlayers = generatePlayers(playersData, tournamentId)

        // Подготовка игроков (добавляем Bye если нужно)
        val preparedPlayers = preparePlayersForBerger(initialPlayers)

        if (tournamentFormat=="Круговая система") {
            bergerTable = bergerTable(preparedPlayers.size)
            maxPossibleCountTour =  preparedPlayers.size - 1
        }
        else maxPossibleCountTour = intent.getIntExtra("tournament_numberTours", preparedPlayers.size-1)

        currentPlayers = preparedPlayers.toMutableList()
        playersHistory.add(currentPlayers)
        // Отображение текущего состояния
        updateUI()


        buttonBack.setOnClickListener { finish() }

        buttonLeft.setOnClickListener {
            if (countTour > 0) {
                countTour--
                tournamentCountTours.text = countTour.toString()
                restorePlayersState()
                updateUI()
            }
        }

        buttonRight.setOnClickListener {
            if (countTour < maxPossibleCountTour) {
                if (areAllResultsSubmitted()) {
                    // Сначала сохраняем текущие результаты
                    if (countTour <= toursHistory.size && countTour>0) {
                        saveCurrentTourResults()
                    }

                    countTour++
                    tournamentCountTours.text = countTour.toString()

                    if (countTour > toursHistory.size) {
                        generateNewTour()
                    } else {
                        restorePlayersState()
                    }
                    updateUI()
                } else {
                    Toast.makeText(this@Tours, "Не все результаты выставлены", Toast.LENGTH_LONG).show()
                }
            }
            else{
                saveCurrentTourResults()
                tournamentCountTours.text = "Конец"
            }
        }

        buttonStatistic.setOnClickListener {
            isStatistic = !isStatistic // Переключаем состояние

            if (isStatistic) {
                listPlayersLabel.text="Статистика текущего тура:"
                buttonExport.visibility = View.VISIBLE
                scrollPairs.visibility = View.GONE
                scrollStatistics.visibility = View.VISIBLE
                populatePlayerTable(currentPlayers) // Заполняем таблицу статистики
            } else {
                listPlayersLabel.text="Список игровых пар:"
                buttonExport.visibility = View.GONE
                scrollPairs.visibility = View.VISIBLE
                scrollStatistics.visibility = View.GONE
                updateUI() // Обновляем основную таблицу с парами
            }
        }
        buttonExport.setOnClickListener {
            exportPlayersToExcel(currentPlayers)
        }
    }

    private fun getPlayers(currentTournamentID:Int) {
        RetrofitClient.apiService.getPlayersByTournamentId(currentTournamentID).enqueue(object : Callback<List<Player>> {
            override fun onResponse(call: Call<List<Player>>, response: Response<List<Player>>) {
                if (response.isSuccessful) {
                    response.body()?.let { players ->
                        initialPlayers = players.toMutableList()
                    }
                } else {
                    Toast.makeText(this@Tours, "Ошибка: ${response.message()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<List<Player>>, t: Throwable) {
                Toast.makeText(this@Tours, "Ошибка сети: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun exportPlayersToExcel(players: List<Player>) {
    try {
        // Фильтруем и сортируем игроков
        val filteredPlayers = players.filter { it.id != -1 }
        val sortedPlayers = filteredPlayers.sortedByDescending { it.points }

        // Создаем книгу Excel
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Статистика игроков турнира")

        // Создаем стиль для заголовков
        val headerStyle = workbook.createCellStyle().apply {
            val font = workbook.createFont().apply {
                bold = true
                color = IndexedColors.WHITE.index
            }
            setFont(font)
            fillForegroundColor = IndexedColors.DARK_BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
        }

        // Заголовки столбцов
        val headers = arrayOf("№",  "Очки", "Бухгольц", "ФИО", "Рейтинг")
        val headerRow = sheet.createRow(0)
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).apply {
                setCellValue(header)
                cellStyle = headerStyle
            }
        }

        // Заполняем данными
        sortedPlayers.forEachIndexed { index, player ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue((index + 1).toDouble())
            row.createCell(1).setCellValue(player.points.toDouble())
            row.createCell(2).setCellValue(player.coefficientPoints.toDouble())
            row.createCell(3).setCellValue(player.fullName ?: "")
            row.createCell(4).setCellValue(player.rate.toDouble())
        }

        // Автонастройка ширины столбцов
//        for (i in 0..4) {
////            sheet.autoSizeColumn(i)
//        }

        // Сохраняем файл
        val fileName = "tournament_stats_${System.currentTimeMillis()}.xlsx"
        val file = File(cacheDir, fileName)
        FileOutputStream(file).use { fos ->
            workbook.write(fos)
            workbook.close()
        }

        // Отправляем файл
        shareExcelFile(file)

    } catch (e: Exception) {
        Toast.makeText(
            this,
            "Ошибка при экспорте: ${e.localizedMessage}",
            Toast.LENGTH_LONG
        ).show()
    }
}

    private fun shareExcelFile(file: File) {
        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Статистика турнира")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, "Поделиться статистикой"))
    }

    private fun populatePlayerTable(playersList: List<Player>) {
        val tableLayout = findViewById<TableLayout>(R.id.tableStatisticLayout)

        // Очищаем предыдущие строки (кроме заголовка)
        val childCount = tableLayout.childCount
        if (childCount > 1) {
            tableLayout.removeViews(1, childCount - 1)
        }

        // Фильтруем Bye-игрока и сортируем по очкам и рейтингу
        val filteredPlayers = playersList.filter { it.id != -1 }
        val sortedPlayers = filteredPlayers.sortedWith(
            compareByDescending<Player> { it.points }
                .thenByDescending { it.coefficientPoints }
        )

        // Добавляем строки для каждого игрока
        sortedPlayers.forEachIndexed { index, player ->
            val tableRow = TableRow(this).apply {
                layoutParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                )
            }

            // Порядковый номер
            tableRow.addView(createCell((index + 1).toString(), 0.3f, true))

            // Очки
            tableRow.addView(createCell(player.points.toString(), 0.35f))

            // Очки Бухгольца
            tableRow.addView(createCell(player.coefficientPoints.toString(), 0.35f))

            // ФИО
            tableRow.addView(createCell(player.fullName, 0.7f))

            // Рейтинг
            tableRow.addView(createCell(player.rate.toString(), 0.5f))

            tableLayout.addView(tableRow)
        }
    }

    private fun generateNewTour() {
        val newPlayers = currentPlayers.map { player ->
            player.copy(prevOpponents = player.prevOpponents.toMutableSet())
        }.toMutableList()

        val newGames = mutableListOf<Game>()

        if (tournamentFormat == "Круговая система") {
            bergerTable[countTour - 1].forEachIndexed { index, (whitePos, blackPos) ->
                val whitePlayer = newPlayers[whitePos - 1]
                val blackPlayer = newPlayers[blackPos - 1]
                val result = if (whitePlayer.id == -1 || blackPlayer.id == -1) {
                    if (whitePlayer.id == -1) "0 - 1" else "1 - 0"
                } else "*"

                val game = blackPlayer.id?.let {
                    whitePlayer.id?.let { it1 ->
                        Game(
                            id = null,
                            numberTable = index + 1,
                            whiteLogin = it1,  // Сохраняем ID
                            blackLogin = it,  // Сохраняем ID
                            result = result
                        )
                    }
                }

                if (whitePlayer.id != -1 && blackPlayer.id != -1) {
                    blackPlayer.id?.let { whitePlayer.prevOpponents.add(it) }
                    whitePlayer.id?.let { blackPlayer.prevOpponents.add(it) }
                }

                if (game != null) {
                    newGames.add(game)
                }
            }
        } else {
            PairingGenerator().generatePairings(newPlayers, countTour, tournamentFormat)
                .forEachIndexed { index, (white, black) ->
                    val game = white.id?.let {
                        black.id?.let { it1 ->
                            Game(
                                id = null,
                                numberTable = index + 1,
                                whiteLogin = it,
                                blackLogin = it1,
                                result = if (white.id == -1 || black.id == -1) "1 - 0" else "*"
                            )
                        }
                    }

                    if (white.id != -1 && black.id != -1) {
                        black.id?.let { white.prevOpponents.add(it) }
                        white.id?.let { black.prevOpponents.add(it) }
                    }

                    if (game != null) {
                        newGames.add(game)
                    }
                }
        }

        toursHistory.add(newGames)
        playersHistory.add(newPlayers)
        currentPlayers = newPlayers
    }

    private fun restorePlayersState() {
        if (countTour >= playersHistory.size) return

        currentPlayers = playersHistory[countTour].map {
            it.copy(prevOpponents = it.prevOpponents.toMutableSet())
        }.toMutableList()
    }

    private fun generatePlayers(
        namesWithRatings: List<Pair<String, Int>>,
        tournamentId: Int
    ): List<Player> {
        return namesWithRatings.mapIndexed { index, (name, rating) ->
            Player(
                id = index + 1, // или другой способ генерации ID
                login = null,
                tournamentId = tournamentId,
                fullName = name,
                teamName = null,
                points = 0f,
                coefficientPoints = 0f,
                rate = rating
            )
        }.sortedByDescending { it.rate }
    }

    private fun saveCurrentTourResults() {
        // Проверяем, что тур существует в истории
        if (countTour == 0 || countTour > toursHistory.size) return

        val tableLayout: TableLayout = findViewById(R.id.tableLayout)
        val currentGames = toursHistory[countTour - 1].toMutableList()

        for (i in 1 until tableLayout.childCount) {
            val row = tableLayout.getChildAt(i) as TableRow
            if (row.childCount > 5) {
                when (val view = row.getChildAt(5)) {
                    is Spinner -> {
                        val result = view.selectedItem.toString()
                        if (i - 1 < currentGames.size) {
                            currentGames[i - 1].result = result
                            applyGameResult(currentGames[i - 1])
                        }
                    }
                    is TextView -> {
                        val result = view.text.toString()
                        currentGames[i - 1].result = result
                        applyGameResult(currentGames[i - 1])
                    }
                }
            }
        }

        toursHistory[countTour - 1] = currentGames
    }

    private fun applyGameResult(game: Game) {
        // Находим игроков в currentPlayers по ID
        val whitePlayer = currentPlayers.find { it.id == game.whiteLogin }
        val blackPlayer = currentPlayers.find { it.id == game.blackLogin }

        if (whitePlayer != null) {
            if (blackPlayer != null) {
                when (game.result) {
                    "1 - 0" -> {
                        whitePlayer.points += 1f
                        blackPlayer.points += 0f
                        whitePlayer.coefficientPoints += blackPlayer.points
                    }

                    "0 - 1" -> {
                        whitePlayer.points += 0f
                        blackPlayer.points += 1f
                        blackPlayer.coefficientPoints += whitePlayer.points
                    }

                    "0.5 - 0.5" -> {
                        whitePlayer.points += 0.5f
                        blackPlayer.points += 0.5f
                    }

                    "0 - 0" -> {
                        // Ничего не добавляем
                    }
                }
            }
        }
    }

    private fun updateUI() {
        val tableLayout: TableLayout = findViewById(R.id.tableLayout)
        if (tableLayout.childCount > 1) {
            tableLayout.removeViews(1, tableLayout.childCount - 1)
        }

        if (countTour == 0) {

        } else {
            val games = toursHistory.getOrNull(countTour - 1) ?: listOf()
//            val players = playersHistory.getOrNull(countTour - 1) ?: currentPlayers
            val players = currentPlayers
            games.forEach { game ->
                val tableRow = TableRow(this)

                // Находим игроков по ID
                val whitePlayer = players.find { it.id == game.whiteLogin }
                val blackPlayer = players.find { it.id == game.blackLogin }

                if (whitePlayer != null && blackPlayer != null) {
                    tableRow.addView(
                        createCell(
                            game.numberTable.toString(),
                            0.3f,
                            isLeft = true,
                            padLeft = true
                        )
                    )
                    tableRow.addView(
                        createCell(
                            if (whitePlayer.id == -1) "[Bye]" else whitePlayer.fullName,
                            0.52f
                        )
                    )
                    tableRow.addView(
                        createCell(
                            whitePlayer.points.toString(),
                            0.4f,
                            isLeft = false
                        )
                    )
                    tableRow.addView(
                        createCell(
                            if (blackPlayer.id == -1) "[Bye]" else blackPlayer.fullName,
                            0.55f
                        )
                    )
                    tableRow.addView(
                        createCell(
                            blackPlayer.points.toString(),
                            0.4f,
                            isLeft = false
                        )
                    )

                    if (whitePlayer.id == -1 || blackPlayer.id == -1) {
                        val resultText = TextView(this).apply {
                            text = if (whitePlayer.id == -1) "0 - 1" else "1 - 0"
                            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                            setTextColor(Color.BLACK)
                            gravity = Gravity.CENTER
                            background =
                                ResourcesCompat.getDrawable(resources, R.drawable.table_cell, null)
                            setPadding(6, 6, 6, 6)
                            layoutParams =
                                TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.6f)
                        }
                        tableRow.addView(resultText)
                    } else {
                        tableRow.addView(createResultSpinner(game))
                    }

                    tableLayout.addView(tableRow)
                }
            }
        }
    }


    private fun createCell(text: String, weight: Float, isLeft: Boolean = false, padLeft: Boolean = false): TextView {
        return TextView(this).apply {
            this.text = text
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(Color.BLACK)
            background = ResourcesCompat.getDrawable(resources,
                if (isLeft) R.drawable.table_cell_left else R.drawable.table_cell, null)
            if (padLeft) setPadding(14, 6, 6, 6)
            else setPadding(6, 6, 6, 6)
            gravity = Gravity.CENTER_VERTICAL
            ellipsize = TextUtils.TruncateAt.END
            maxLines = 1
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, weight)
        }
    }

    private fun createResultSpinner(game: Game): Spinner {
        val results = arrayOf("*", "1 - 0", "0 - 1", "0.5 - 0.5", "0 - 0")

        return Spinner(this).apply {
            adapter = ArrayAdapter(this@Tours, R.layout.result_spinner_item, results).apply {
                setDropDownViewResource(R.layout.result_spinner_dropdown_item)
            }

            setSelection(results.indexOf(game.result))
            background = ResourcesCompat.getDrawable(resources, R.drawable.table_cell, null)
            isEnabled = (countTour == toursHistory.size)
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.6f)

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    val newResult = results[position]
                    if (game.result != newResult) {
                        game.result = newResult
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
    }

    private fun areAllResultsSubmitted(): Boolean {
        if (countTour == 0) return true

        // Если это новый тур, который ещё не сгенерирован
        if (countTour > toursHistory.size) return false

        val tableLayout: TableLayout = findViewById(R.id.tableLayout)

        for (i in 1 until tableLayout.childCount) {
            val tableRow = tableLayout.getChildAt(i) as TableRow
            if (tableRow.childCount > 5) {
                val view = tableRow.getChildAt(5)
                when (view) {
                    is Spinner -> {
                        if (view.selectedItem == "*") return false
                    }
                    is TextView -> {
                        // Для Bye-игр всегда считаем что результат установлен
                        if (view.text.toString() == "*") return false
                    }
                }
            }
        }
        return true
    }

    private fun preparePlayersForBerger(players: List<Player>): List<Player> {
        val preparedPlayers = players.toMutableList()

        // Если количество игроков нечетное, добавляем игрока Bye
        if (players.size % 2 != 0) {
            preparedPlayers.add(
                Player(
                    id = -1,
                    login = "Bye",
                    tournamentId = tournamentId,
                    fullName = "Bye",
                    points = 0f,
                    coefficientPoints = 0f,
                    rate = -1
                )
            )
        }

        return preparedPlayers
    }

    private fun bergerTable(n: Int): List<List<Pair<Int, Int>>> {
        var numPlayers = n
        if (n%2!=0) numPlayers+=1

        val p = numPlayers shr 1
        val a = MutableList(numPlayers - 1) { it + 1 }

        val table = mutableListOf<List<Pair<Int, Int>>>()
        val halfTableFirst = mutableListOf<List<Pair<Int, Int>>>()
        val halfTableSecond = mutableListOf<List<Pair<Int, Int>>>()

        for (i in 0 until numPlayers - 1) {
            val round = mutableListOf<Pair<Int, Int>>()
            if (i>=numPlayers/2)
            {
                // Пара с фиксированным игроком numPlayers
                round.add(Pair(numPlayers, a[0]))
                for (j in 1 until p)
                    round.add(Pair(a[j], a[numPlayers - 1 - j]))
                halfTableSecond.add(round)
            }
            else {
                round.add(Pair(a[0], numPlayers))
                for (j in 1 until p)
                    round.add(Pair(a[j], a[numPlayers - 1 - j]))
                halfTableFirst.add(round)
            }

            // Сдвигаем список a: первый элемент уходит в конец
            val first = a.removeAt(0)
            a.add(first)
        }

        val maxSize = maxOf(halfTableFirst.size, halfTableSecond.size)

        for (i in 0 until maxSize) {
            if (i < halfTableFirst.size) {
                table.add(halfTableFirst[i])
            }
            if (i < halfTableSecond.size) {
                table.add(halfTableSecond[i])
            }
        }

        return table
    }

}
