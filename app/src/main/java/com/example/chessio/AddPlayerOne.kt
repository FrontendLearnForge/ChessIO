package com.example.chessio
//
//import android.content.Intent
//import android.os.Bundle
//import android.widget.Button
//import android.widget.EditText
//import android.widget.ImageView
//import android.widget.Toast
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//
//class AddPlayerOne : AppCompatActivity() {
//    private var tournamentId: Int = 0 // Переменная для хранения ID турнира
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_add_player_one)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
//        tournamentId = intent.getIntExtra("TOURNAMENT_ID", 0)
//
//        val playerName: EditText = findViewById(R.id.name_player)
//        val playerAddress: EditText = findViewById(R.id.adress_player)
//        val playerTeam: EditText = findViewById(R.id.team_player)
//        val buttonAdd: Button = findViewById(R.id.button_add)
//
//        buttonAdd.setOnClickListener {
//            val fullName = playerName.text.toString().trim()
//            val address = playerAddress.text.toString().trim()
//            val team = playerTeam.text.toString().trim()
//
//            if (fullName.isEmpty() || address.isEmpty() || team.isEmpty()) {
//                Toast.makeText(this, "Не все поля заполнены", Toast.LENGTH_LONG).show()
//            } else {
//                val player = Player(id = null, login=null, tournamentId=tournamentId, fullName=fullName,
//                     teamName =null, points=0.0f, buhPoints=0.0f, rate=1000)
//
//                // Отправка данных на сервер
//                RetrofitClient.apiService.addPlayer(player).enqueue(object : Callback<Player> {
//                    override fun onResponse(call: Call<Player>, response: Response<Player>) {
//                        if (response.isSuccessful) {
//                            Toast.makeText(this@AddPlayerOne, "Участник добавлен!", Toast.LENGTH_LONG).show()
//                            playerName.text.clear()
//                            playerAddress.text.clear()
//                            playerTeam.text.clear()
//
//                            val intent = Intent(this@AddPlayerOne, AddPlayers::class.java)
//                            intent.putExtra("TOURNAMENT_ID", tournamentId) // Передаем tournamentId, если нужно
//                            startActivity(intent)
//                            finish() // Закрываем текущую активность, если необходимо
//
//                        } else {
//                            Toast.makeText(this@AddPlayerOne, "Ошибка: ${response.message()}", Toast.LENGTH_LONG).show()
//                        }
//                    }
//
//                    override fun onFailure(call: Call<Player>, t: Throwable) {
//                        Toast.makeText(this@AddPlayerOne, "Ошибка сети: ${t.message}", Toast.LENGTH_LONG).show()
//                    }
//                })
//            }
//        }
//
//
//        val buttonBack: ImageView = findViewById(R.id.imageView_button_back)
//        buttonBack.setOnClickListener {
////            val intent = Intent(this, AddPlayers::class.java)
////            intent.putExtra("TOURNAMENT_ID", tournamentId)
////            startActivity(intent)
//            finish()
//        }
//    }
//}
