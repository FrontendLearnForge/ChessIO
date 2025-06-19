package com.example.chessio

import com.google.gson.annotations.SerializedName

data  class Team(
    @SerializedName("id") val id: Int,
    @SerializedName("tournament_id") val tournamentId: Int,
    @SerializedName("name") val teamName: String,
    @SerializedName("points") var points: Float,
    @SerializedName("coefficient_points") var coefficientPoints: Float,
    @Transient var currentPlayers: MutableList<Player>
)