package com.example.chessio
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("api/users")
    fun getUsers(): Call<List<User>>
    @POST("api/users")
    fun registerUser (@Body user: User): Call<User>

    @Multipart
    @POST("api/upload-image")
    fun uploadImage(@Part image: MultipartBody.Part): Call<ImageResponse>

    @DELETE("api/users/{login}")
    fun deleteUser(
        @Path("login") login: String,
        @Query("userLogin") userLogin: String
    ): Call<Void>
    @POST("api/login")
    fun enterUser (@Body user: EnterUser): Call<User>
    @GET("api/users/{login}")
    fun getUserByLogin (@Path("login") login: String): Call<User>
    @PUT("api/users")
    fun updateUser (@Body user: User): Call<Void>

    @PUT("api/users/role")
    fun updateUserRole(
        @Query("login") login: String,
        @Query("newRole") newRole: String,
        @Query("adminLogin") adminLogin: String
    ): Call<Void>

    //Турниры
    @POST("api/tournaments")
    fun createTournament(@Body tournament: Tournament): Call<Tournament>
    @GET("api/tournaments")
    fun getTournaments(): Call<List<Tournament>>
    @GET("api/tournaments/{id}")
    fun getTournamentById(@Path("id") tournamentId: Int): Call<Tournament>
    @DELETE("api/tournaments/{id}")
    fun deleteTournament(
        @Path("id") tournamentId: Int,
        @Query("userLogin") userLogin: String
    ): Call<Void>

    // Метод для получения всех участников турнира по его ID
    @GET("api/tournaments/{id}/players")
    fun getPlayersByTournamentId(@Path("id") tournamentId: Int): Call<List<Player>>
//    //Заявки
//    @POST("api/applications")
//    fun createApplication(@Body application: Application): Call<Application>

    @POST("/api/tournaments/{tournamentId}/applications")
    fun createApplication(
        @Path("tournamentId") tournamentId: Int,
        @Body request: CreateApplicationRequest
    ): Call<Application>

    // Получение заявок пользователя на конкретный турнир
    @GET("api/applications/user/{userLogin}/tournament/{tournamentId}")
    fun getUserApplications(
        @Path("userLogin") userLogin: String,
        @Path("tournamentId") tournamentId: Int
    ): Call<List<Application>>

    @GET("api/applications/tournament/{tournamentId}")
    fun getApplicationsByTournament(@Path("tournamentId") tournamentId: Int): Call<List<Application>>


    @GET("api/applications/tournament/{tournamentId}")
    fun getPendingApplicationsByTournament(
        @Path("tournamentId") tournamentId: Int,
        @Query("status") status: String = "На рассмотрении"
    ): Call<List<Application>>

    @PATCH("/api/applications/{id}/")
    fun changeApplicationStatus(
        @Path("id") id: Int,
        @Body statusUpdate: StatusUpdateRequest
    ): Call<Application>

    //Добавление игрока из формы
    @POST("api/players")
    fun addPlayer(@Body player: Player): Call<Player>

    //Добавление игрока по заявке
    @POST("/api/tournaments/{tournamentId}/players")
    fun addPlayer(
        @Path("tournamentId") tournamentId: Int,
        @Body request: CreatePlayerRequest
    ): Call<Player>

    @DELETE("/api/tournaments/{tournamentId}/players/{playerId}")
    fun deletePlayer(
        @Path("tournamentId") tournamentId: Int,
        @Path("playerId") playerId: Int,
        @Query("userLogin") userLogin: String
    ): Call<Void>

    //Уведомления
    @GET("api/notifications")
    suspend fun getNotifications(@Query("userLogin") userLogin: String): List<Notification>

    @PUT("api/notifications/{id}/mark-as-read")
    suspend fun markAsRead(@Path("id") notificationId: Int): Response<Void>
}
