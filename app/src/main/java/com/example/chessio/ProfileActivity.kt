package com.example.chessio

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
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

class ProfileActivity : AppCompatActivity() {
    private lateinit var avatarImageView: ImageView
    private lateinit var getImageLauncher: ActivityResultLauncher<Intent>
    private var selectedImageUri: Uri? = null // Добавляем переменную для хранения URI изображения

    private var originalUser: User? = null

    private lateinit var currentUserLogin: String
    private lateinit var currentUserRole: String

    private lateinit var profileLogin: String

    private lateinit var loginView: TextView
    private lateinit var loginEdit: EditText
    private lateinit var nameView: TextView
    private lateinit var nameEdit: EditText
    private lateinit var addressView: TextView
    private lateinit var addressEdit: EditText

    private lateinit var userDateBirth: TextView
    private lateinit var userRate: TextView
    private lateinit var userRole: TextView

    private lateinit var roleSpinner: Spinner
    private var isAdminEditingOtherUser = false

    private lateinit var userPasswordLabel: TextView
    private lateinit var passwordEdit: EditText
    private lateinit var userPasswordAgainLabel: TextView
    private lateinit var userPasswordAgain: EditText

    private lateinit var buttonBack: ImageView
    private lateinit var buttonEdit: Button
    private lateinit var buttonCancel: Button
    private var isEditing = false

    private var role: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        avatarImageView = findViewById(R.id.imageView_profile)

        loginView = findViewById(R.id.login_view)
        loginEdit = findViewById(R.id.login_edit)
        nameView = findViewById(R.id.name_view)
        nameEdit = findViewById(R.id.name_edit)
        addressView = findViewById(R.id.adress_view)
        addressEdit = findViewById(R.id.adress_edit)

        userDateBirth = findViewById(R.id.date_reg)
        userRate=findViewById(R.id.rate_reg)
        userRole=findViewById(R.id.role_reg)

        roleSpinner = findViewById(R.id.spinner_role)

        userPasswordLabel=findViewById(R.id.password_reg_label)
        passwordEdit = findViewById(R.id.password_edit)
        userPasswordAgainLabel=findViewById(R.id.password_again_label)
        userPasswordAgain = findViewById(R.id.password_again_edit)


        buttonBack = findViewById(R.id.imageView_button_back)
        buttonBack.setOnClickListener {
            finish()
        }

        buttonEdit = findViewById(R.id.button_edit)
        buttonEdit.setOnClickListener {
            if (isEditing) {
                isEditing=false
                saveUserData()
            } else {
                isEditing=true
                toggleEditMode(isEditing)
            }
        }

        buttonCancel = findViewById(R.id.button_cancel)
        buttonCancel.setOnClickListener {
            cancelEditing()
        }

        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        currentUserLogin = sharedPreferences.getString("current_user_login", null) ?: run {
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        currentUserRole = sharedPreferences.getString("current_user_role", "guest") ?: "guest"

        profileLogin = intent.getStringExtra("user_login") ?: currentUserLogin
        isAdminEditingOtherUser = currentUserRole == "Администратор" && profileLogin != currentUserLogin
        if (isAdminEditingOtherUser || profileLogin == currentUserLogin)
        {
            buttonEdit.visibility = View.VISIBLE
        }
        else {
            buttonEdit.visibility = View.GONE
        }

        setupImagePicker()
        loadUserData(profileLogin)
    }

    private fun setupImagePicker() {
        getImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedImageUri = uri
                    Glide.with(this@ProfileActivity)
                        .load(uri)
                        .circleCrop()
                        .into(avatarImageView)
                }
            }
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

    private fun loadUserData(login: String) {
        RetrofitClient.apiService.getUserByLogin(login).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
              if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        originalUser = user

                        // Загрузка изображения
                        if (user.imageUrl.isNotEmpty()) {
                            Glide.with(this@ProfileActivity)
                                .load(user.imageUrl)
                                .placeholder(R.drawable.img_reg)
                                .error(R.drawable.img_reg)
                                .circleCrop()
                                .into(avatarImageView)
                        } else {
                            avatarImageView.setImageResource(R.drawable.img_reg)
                        }

                        loginView.text = user.login
                        nameView.text = user.username
                        addressView.text = user.address
                        userDateBirth.text = user.dateOfBirth
                        userRate.text = user.rate.toString()
                        userRole.text=user.role
                        passwordEdit.setText(user.password)
                    } else {
                        Toast.makeText(this@ProfileActivity, "Пользователь не найден", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ProfileActivity, "Ошибка: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun toggleEditMode(isEditable: Boolean) {
        if (isAdminEditingOtherUser) {
            // Режим администратора для редактирования другого пользователя
            if (isEditable) {
                // Показываем только спиннер для роли
                setupRoleSpinner()
                userRole.visibility = View.GONE
                roleSpinner.visibility = View.VISIBLE
            } else {
                // Возвращаем обычный вид
                userRole.visibility = View.VISIBLE
                roleSpinner.visibility = View.GONE
            }
        } else {

// Обычный режим редактирования
        val fields = listOf(
            Pair(findViewById<TextView>(R.id.login_view), findViewById<EditText>(R.id.login_edit)),
            Pair(findViewById(R.id.name_view), findViewById(R.id.name_edit)),
            Pair(findViewById(R.id.adress_view), findViewById(R.id.adress_edit))
        )

        fields.forEach { (view, edit) ->
            if (isEditable) {
                view.visibility = View.GONE
                edit.visibility = View.VISIBLE
                edit.setText(view.text)
            } else {
                view.visibility = View.VISIBLE
                edit.visibility = View.GONE
                view.text = edit.text.toString()
            }
        }

        if (isEditable) {
            userDateBirth.setOnClickListener { showDatePicker() }
            avatarImageView.setOnClickListener { openGallery() }
        } else {
            userDateBirth.setOnClickListener(null)
            avatarImageView.setOnClickListener(null)
        }

        userPasswordLabel.visibility = if (isEditable) View.VISIBLE else View.GONE
        passwordEdit.visibility = if (isEditable) View.VISIBLE else View.GONE

        userPasswordAgainLabel.visibility = if (isEditable) View.VISIBLE else View.GONE
        userPasswordAgain.visibility = if (isEditable) View.VISIBLE else View.GONE
        }

        // Общие элементы управления
        buttonEdit.text = if (isEditable) "Сохранить" else "Редактировать профиль"

        if (isEditable) {
            buttonEdit.animate().translationX(10f).start()
            buttonCancel.visibility = View.VISIBLE
            buttonCancel.alpha = 0f
            buttonCancel.animate().alpha(1f).start()
        } else {
            buttonEdit.animate().translationX(0f).start()
            buttonCancel.animate().alpha(0f).withEndAction {
                buttonCancel.visibility = View.GONE
            }.start()
        }
        isEditing = isEditable
    }

    private fun cancelEditing() {
        originalUser?.let { user ->
            loginView.text = user.login
            nameView.text = user.username
            addressView.text = user.address
            userDateBirth.text = user.dateOfBirth
            userRate.text = user.rate.toString()
            userRole.text = user.role
            passwordEdit.setText(user.password)

            userPasswordAgain.text.clear()

            if (user.imageUrl.isNotEmpty()) {
                Glide.with(this@ProfileActivity)
                    .load(user.imageUrl)
                    .placeholder(R.drawable.img_reg)
                    .error(R.drawable.img_reg)
                    .circleCrop()
                    .into(avatarImageView)
            } else {
                avatarImageView.setImageResource(R.drawable.img_reg)
            }
        }
        isEditing = false
        toggleEditMode(false)
        selectedImageUri = null
    }

    private fun uploadImage(callback: (String?) -> Unit) {
        selectedImageUri?.let { uri ->
            createTempImageFile(uri)?.let { file ->
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

                RetrofitClient.apiService.uploadImage(imagePart).enqueue(object : Callback<ImageResponse> {
                    override fun onResponse(call: Call<ImageResponse>, response: Response<ImageResponse>) {
                        callback(response.body()?.imageUrl)
                    }

                    override fun onFailure(call: Call<ImageResponse>, t: Throwable) {
                        callback(null)
                    }
                })
            } ?: callback(null)
        } ?: callback(null)
    }

    private fun createTempImageFile(uri: Uri): File? {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                File.createTempFile("avatar_", ".jpg", cacheDir).apply {
                    outputStream().use { output -> inputStream.copyTo(output) }
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun saveUserData() {
        if (isAdminEditingOtherUser) {
            updateUserRole()
            return
        }

        val login = loginEdit.text.toString().trim()
        val name = nameEdit.text.toString().trim()
        val address = addressEdit.text.toString().trim()
        val password = passwordEdit.text.toString().trim()
        val passwordAgain = userPasswordAgain.text.toString().trim()

        when {
            login.isEmpty() || name.isEmpty() || address.isEmpty() || password.isEmpty() || passwordAgain.isEmpty() ->
                Toast.makeText(this, "Не все поля заполнены", Toast.LENGTH_LONG).show()
            password != passwordAgain ->
                Toast.makeText(this, "Введённые пароли не совпадают", Toast.LENGTH_LONG).show()
            else -> {
                if (selectedImageUri != null) {
                    uploadImage { imageUrl ->
                        if (imageUrl != null) {
                            updateUserProfile(login, name, address, password, imageUrl)
                        }
                    }
                } else {
                    updateUserProfile(login, name, address, password, originalUser?.imageUrl ?: "")
                }
            }
        }
    }

    private fun updateUserProfile(login: String, name: String, address: String, password: String, imageUrl: String) {
        val updatedUser = originalUser?.copy(
            login = login,
            username = name,
            address = address,
            password = password,
            imageUrl = imageUrl
        ) ?: return

        RetrofitClient.apiService.updateUser(updatedUser).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    originalUser = updatedUser
                    userPasswordAgain.text.clear()
                    toggleEditMode(false)
                    Toast.makeText(this@ProfileActivity, "Данные успешно обновлены", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ProfileActivity, "Ошибка: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setupRoleSpinner() {
        val roles = arrayOf("Игрок", "Организатор", "Администратор")
        val adapter = ArrayAdapter(this, R.layout.spinner_item, roles).apply {
            setDropDownViewResource(R.layout.spinner_dropdown_item)
        }
        roleSpinner.adapter = adapter

        // Устанавливаем текущую роль пользователя
        val currentRole = userRole.text.toString()
        val position = roles.indexOf(currentRole)
        if (position >= 0) {
            roleSpinner.setSelection(position)
        }

        roleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                role = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>)
            {
                role = ""
            }
        }
    }

    private fun updateUserRole() {
        val newRole = role
        val userLogin = loginView.text.toString()

        if (newRole.isEmpty() || userLogin.isEmpty()) {
            Toast.makeText(this, "Ошибка: не все данные заполнены", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.apiService.updateUserRole(userLogin, newRole, currentUserLogin).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ProfileActivity, "Роль пользователя обновлена", Toast.LENGTH_SHORT).show()
                    userRole.text = newRole
                    toggleEditMode(false)
                } else {
                    Toast.makeText(this@ProfileActivity, "Ошибка: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
