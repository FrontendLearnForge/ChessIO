package com.example.chessio

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddPlayerDialog(
//    private var currentUserLogin: String? = null,
    private val tournamentId: Int,
    private val isTeamTournament: Boolean,
    private val onPlayerAdded: () -> Unit
) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_add_player_one, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val playerName: EditText = view.findViewById(R.id.name_player)
        val playerRate: EditText = view.findViewById(R.id.rate_player)
        val playerTeamLabel: TextView = view.findViewById(R.id.team_player_label)
        val playerTeam: EditText = view.findViewById(R.id.team_player)
        val buttonAdd: Button = view.findViewById(R.id.button_add)
        val buttonBack: ImageView = view.findViewById(R.id.imageView_button_back)

        if (isTeamTournament) {
            playerTeamLabel.visibility = View.VISIBLE
            playerTeam.visibility = View.VISIBLE
        } else {
            playerTeamLabel.visibility = View.GONE
            playerTeam.visibility = View.GONE
        }

        buttonAdd.setOnClickListener {
            val fullName = playerName.text.toString().trim()
            val rate = playerRate.text.toString().trim()
            val team = if (isTeamTournament) playerTeam.text.toString().trim() else ""

            if (fullName.isEmpty() || rate.isEmpty() || (isTeamTournament && team.isEmpty())) {
                Toast.makeText(context, "Не все поля заполнены", Toast.LENGTH_LONG).show()
            } else {

                val player = Player(
                    login = null,
                    tournamentId = tournamentId,
                    fullName = fullName,
                    teamName = if (isTeamTournament) team else null,
                    points = 0.0f,
                    coefficientPoints = 0.0f,
                    rate = rate.toInt()
                )

                // Отправка на сервер
                RetrofitClient.apiService.addPlayer(player).enqueue(object : Callback<Player> {
                    override fun onResponse(call: Call<Player>, response: Response<Player>) {
                        if (response.isSuccessful) {
                            Toast.makeText(context, "Участник добавлен!", Toast.LENGTH_SHORT).show()
                            onPlayerAdded()
                            dismiss()
                        } else {
                            val error = when (response.code()) {
                                409 -> "Этот пользователь уже зарегистрирован в турнире"
                                else -> "Ошибка сервера: ${response.message()}"
                            }
                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<Player>, t: Throwable) {
                        Toast.makeText(context, "Ошибка сети: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
            }
        }

        buttonBack.setOnClickListener {
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        return dialog
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.75).toInt()
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog?.window?.setLayout(width, height)
    }
}