package com.example.chessio

import com.google.gson.annotations.SerializedName

data class Player(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("login") val login: String? = null,
    @SerializedName("tournament_id") val tournamentId: Int,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("team_name") val teamName: String? = null,
    @SerializedName("points") var points: Float,
    @SerializedName("buh_points") var coefficientPoints: Float,
    @SerializedName("rate") var rate: Int,
    @SerializedName("color_balance") var colorBalance: Int = 0, // +1 за белые, -1 за черные
    @Transient val prevOpponents: MutableSet<Int> = mutableSetOf(),
)

data class CreatePlayerRequest(
    @SerializedName("user_login") val userLogin: String,
    @SerializedName("team_name") val teamName: String?
)
