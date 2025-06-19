package com.example.chessio

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("login") var login: String,
    @SerializedName("image_url") var imageUrl: String,
    @SerializedName("username") var username: String,
    @SerializedName("address") var address: String,
    @SerializedName("date_of_birth") var dateOfBirth: String,
    @SerializedName("rate") var rate: Int,
    @SerializedName("role") var role: String,
    @SerializedName("password") var password: String
)

data class EnterUser (
    @SerializedName("login") val login: String,
    @SerializedName("password") val password: String
)

data class ImageResponse(
    @SerializedName("image_url") val imageUrl: String
)