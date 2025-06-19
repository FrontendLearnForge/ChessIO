package com.example.chessio

import com.google.gson.annotations.SerializedName

data class Tour(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("number_tour") val numberTour: Int,
    @SerializedName("tournament_id") val tournamentId: Int,
    @Transient var currentGames: MutableList<Game>,
    @Transient var currentPlayers: MutableList<Player>
)

