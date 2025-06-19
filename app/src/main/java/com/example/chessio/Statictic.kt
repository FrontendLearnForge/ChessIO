package com.example.chessio

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Statictic : AppCompatActivity() {

    private lateinit var playersList: List<Player>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_statictic)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val buttonBack: Button = findViewById(R.id.button_back)
        buttonBack.setOnClickListener {
            val intent = Intent(this, Tours::class.java)
            startActivity(intent)
        }
//        playersList = intent.extras?.getParcelableArrayList("CURRENT_PLAYERS") ?: emptyList()

        // Заполняем таблицу игроками
        populatePlayerTable(playersList)
    }

    private fun populatePlayerTable(playersList: List<Player>) {
        // Находим TableLayout в вашем макете
        val tableLayout = findViewById<TableLayout>(R.id.tableLayout)

        // Очищаем предыдущие строки (если есть)
        tableLayout.removeViewsInLayout(1, tableLayout.childCount - 1) // Удаляем все строки, кроме заголовка

        // Сортируем игроков по очкам в порядке убывания
        val sortedPlayers = playersList.sortedByDescending { it.points }

        // Добавляем строки для каждого игрока
        sortedPlayers.forEachIndexed { index, player ->
            val tableRow = TableRow(this).apply {
                layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
            }

            // Номер игрока
            val playerNumber = TextView(this).apply {
                text = (index + 1).toString()
                layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT)
                background = ResourcesCompat.getDrawable(resources, R.drawable.table_cell_background, null) // Применяем фон
                setPadding(6, 0, 0, 0)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                setTextColor(ContextCompat.getColor(this@Statictic, R.color.black)) // Устанавливаем цвет текста
            }

            // Очки игрока
            val playerPoints = TextView(this).apply {
                text = player.points.toString()
                layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT)
                background = ResourcesCompat.getDrawable(resources, R.drawable.table_cell_background, null) // Применяем фон
                setPadding(6, 0, 0, 0)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                setTextColor(ContextCompat.getColor(this@Statictic, R.color.black)) // Устанавливаем цвет текста
            }

            // ФИО игрока
            val playerName = TextView(this).apply {
                text = player.fullName
                layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT)
                background = ResourcesCompat.getDrawable(resources, R.drawable.table_cell_background, null) // Применяем фон
                setPadding(6, 0, 0, 0)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                setTextColor(ContextCompat.getColor(this@Statictic, R.color.black)) // Устанавливаем цвет текста
            }

            // Команда игрока
            val playerTeam = TextView(this).apply {
                text = player.teamName // Предполагается, что у вас есть поле team в классе Player
                layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT)
                background = ResourcesCompat.getDrawable(resources, R.drawable.table_cell_background, null) // Применяем фон
                setPadding(6, 0, 0, 0)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                setTextColor(ContextCompat.getColor(this@Statictic, R.color.black)) // Устанавливаем цвет текста
            }

            // Адрес игрока
            val playerAddress = TextView(this).apply {
                text = player.rate.toString() // Предполагается, что у вас есть поле address в классе Player
                layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT)
                background = ResourcesCompat.getDrawable(resources, R.drawable.table_cell_background, null) // Применяем фон
                setPadding(6, 0, 0, 0)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                setTextColor(ContextCompat.getColor(this@Statictic, R.color.black)) // Устанавливаем цвет текста
            }

            // Добавляем все TextView в строку
            tableRow.addView(playerNumber)
            tableRow.addView(playerPoints)
            tableRow.addView(playerName)
            tableRow.addView(playerTeam)
            tableRow.addView(playerAddress)

            // Добавляем строку в TableLayout
            tableLayout.addView(tableRow)
        }
    }

}