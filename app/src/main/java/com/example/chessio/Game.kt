package com.example.chessio

import com.google.gson.annotations.SerializedName

data class Game(
    @SerializedName("id") val id: Int? = null,
//    @SerializedName("tournament_id") val tournamentId: Int,
//    @SerializedName("tour_id")val tourId: Int,
    @SerializedName("number_table")val numberTable: Int,
    @Transient val whiteLogin: Int,  // Храним только ID
    @Transient val blackLogin: Int,  // Храним только ID
//    @SerializedName("white_name") val whiteName: String,
//    @SerializedName("black_name") val blackName: String,
    @SerializedName("result") var result: String
)
