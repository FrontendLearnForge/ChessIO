package com.example.chessio

import com.google.gson.annotations.SerializedName

data class Tournament(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("date_start") val dateStart: String,
    @SerializedName("date_end") val dateEnd: String,
    @SerializedName("name") val name: String,
    @SerializedName("name_referee") val nameReferee: String,
    @SerializedName("address") val address: String,
    @SerializedName("number_tours") val numberTours: Int,
    @SerializedName("type") val type: String,
    @SerializedName("format") val format: String,
    @SerializedName("organizer_login") val organizerLogin: String,
    @SerializedName("status") val status: String     // "Создан" "Начат" "Закончен"
)