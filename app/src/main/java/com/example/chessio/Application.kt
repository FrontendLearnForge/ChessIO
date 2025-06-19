package com.example.chessio

import com.google.gson.annotations.SerializedName

data class Application(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("tournament_id") val tournamentId: Int,
    @SerializedName("user_login") val userLogin: String,
    @SerializedName("user_name") val userName: String,
    @SerializedName("team_name") val teamName: String? = null,
    @SerializedName("rate") val rate: Int,
    @SerializedName("status") val status: String // "На рассмотрении", "Принята", "Отклонена"
)

data class CreateApplicationRequest(
    @SerializedName("user_login") val userLogin: String,
    @SerializedName("team_name") val teamName: String?
)

data class StatusUpdateRequest(
    @SerializedName("status") val status: String
)