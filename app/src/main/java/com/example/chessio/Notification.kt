package com.example.chessio

import com.google.gson.annotations.SerializedName

data class Notification(
    @SerializedName("id") val id: Int,
//    @SerializedName("user_login") val userLogin: String,
    @SerializedName("message") val message: String,
    @SerializedName("tournament_id") val tournamentId: Int?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("is_read") val isRead: Boolean
)
