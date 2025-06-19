package com.example.chessio

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RegisterActivity : AppCompatActivity() {

    private lateinit var avatarImageView: ImageView
    private lateinit var getImageLauncher: ActivityResultLauncher<Intent>
    private var selectedImageUri: Uri? = null

    private lateinit var userLogin: EditText
    private lateinit var userName: EditText
    private lateinit var userAddress: EditText
    private lateinit var userPassword: EditText
    private lateinit var userDateBirth: TextView
    private lateinit var userPasswordAgain: EditText

    private lateinit var buttonBack: ImageView
    private lateinit var buttonReg: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        avatarImageView = findViewById(R.id.imageView_profile)
        userLogin = findViewById(R.id.login_reg)
        userName = findViewById(R.id.name_reg)
        userAddress = findViewById(R.id.adress_reg)
        userPassword = findViewById(R.id.password_reg)
        userDateBirth = findViewById(R.id.date_reg)
        userPasswordAgain = findViewById(R.id.password_again_reg)
        buttonBack = findViewById(R.id.imageView_button_back)
        buttonReg = findViewById(R.id.button_reg)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        avatarImageView.setOnClickListener {
            openGallery()
        }

        getImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedImageUri = uri
                    Glide.with(this@RegisterActivity)
                        .load(uri)
                        .circleCrop()
                        .into(avatarImageView)
                }
            }
        }

        userDateBirth.setOnClickListener {
            showDatePicker()
        }

        buttonReg.setOnClickListener {
            val login = userLogin.text.toString().trim()
            val name = userName.text.toString().trim()
            val address = userAddress.text.toString().trim()
            val date = userDateBirth.text.toString().trim()
            val rate = 1000
            val role = "Игрок"
            val password = userPassword.text.toString().trim()
            val passwordAgain = userPasswordAgain.text.toString().trim()

            if (login.isEmpty() || name.isEmpty() || address.isEmpty() || date.isEmpty() || password.isEmpty() || passwordAgain.isEmpty()) {
                Toast.makeText(this, "Не все поля заполнены", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (password != passwordAgain) {
                Toast.makeText(this, "Введённые пароли не совпадают", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            registerUser(login, name, address, date, rate, role, password)
        }

        buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun registerUser(
        login: String,
        name: String,
        address: String,
        date: String,
        rate: Int,
        role: String,
        password: String
    ) {
        if (selectedImageUri == null) {
            sendUserData(login, name, address, date, rate, role, password, "")
            return
        }
        uploadImage { imageUrl ->
            runOnUiThread {
                if (imageUrl != null) {
                    sendUserData(login, name, address, date, rate, role, password, imageUrl)
                } else {
                    Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    private fun uploadImage(callback: (String?) -> Unit) {
        selectedImageUri?.let { uri ->
            val file = createTempImageFile(uri)
            file?.let {
                val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", it.name, requestFile)

                RetrofitClient.apiService.uploadImage(imagePart).enqueue(object : Callback<ImageResponse> {
                    override fun onResponse(call: Call<ImageResponse>, response: Response<ImageResponse>) {
                        if (response.isSuccessful) {
                            callback(response.body()?.imageUrl)
                        } else {
                            Log.e("UploadImage", "Ошибка сервера: ${response.code()} - ${response.message()}")
                            callback(null)
                        }
                    }

                    override fun onFailure(call: Call<ImageResponse>, t: Throwable) {
                        Log.e("UploadImage", "Ошибка сети: ${t.message}")
                        callback(null)
                    }
                })
            } ?: callback(null)
        } ?: callback(null)
    }

    private fun sendUserData(login: String, name: String, address: String, date: String, rate: Int, role: String, password: String, imageUrl: String) {
        val user = User(
            login = login,
            imageUrl = imageUrl,
            username = name,
            address = address,
            dateOfBirth = date,
            rate = rate,
            role = role,
            password = password
        )

        RetrofitClient.apiService.registerUser(user).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@RegisterActivity, "Регистрация успешна!", Toast.LENGTH_SHORT).show()
                    enterMenu()
                } else {
                    handleRegistrationError(response.code())
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Toast.makeText(this@RegisterActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun handleRegistrationError(statusCode: Int) {
        when (statusCode) {
            400 -> Toast.makeText(this, "Пользователь с таким логином уже существует", Toast.LENGTH_LONG).show()
            500 -> Toast.makeText(this, "Ошибка сервера. Попробуйте позже", Toast.LENGTH_LONG).show()
            else -> Toast.makeText(this, "Неизвестная ошибка: $statusCode", Toast.LENGTH_LONG).show()
        }
    }

    private fun createTempImageFile(uri: Uri): File? {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val file = File.createTempFile("avatar_", ".jpg", cacheDir)
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                file
            }
        } catch (e: Exception) {
            Log.e("ImageFile", "Error creating temp file: ${e.message}")
            null
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png", "image/jpg"))
        }
        getImageLauncher.launch(intent)
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            val selectedDate = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, day)
            }
            val displayFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val formattedDate = displayFormat.format(selectedDate.time)
            userDateBirth.text = formattedDate
        },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun enterMenu() {
        finish()
    }
}