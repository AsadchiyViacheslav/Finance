package com.example.finance

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finance.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val savedUsername = sharedPref.getString("username", null)

        // Если логин уже сохранён — переходим на главный экран
        if (savedUsername != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelper = DatabaseHelper(this)

        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (validateInput(username, password, confirmPassword)) {
                registerUser(username, password)
            }

        }
        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun validateInput(
        username: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        return when {
            username.isEmpty() -> {
                binding.etUsername.error = "Введите логин"
                false
            }
            username.length < 5 -> {
                binding.etUsername.error = "Логин слишком короткий (мин. 5 символов)"
                false
            }
            password.isEmpty() -> {
                binding.etPassword.error = "Введите пароль"
                false
            }
            password.length < 6 -> {
                binding.etPassword.error = "Пароль слишком короткий (мин. 6 символов)"
                false
            }
            password != confirmPassword -> {
                binding.etConfirmPassword.error = "Пароли не совпадают"
                false
            }
            dbHelper.isUsernameTaken(username) -> {
                binding.etUsername.error = "Логин уже занят"
                false
            }
            else -> true
        }
    }

    private fun registerUser(username: String, password: String) {
        val userId = dbHelper.registerUser(username, password)

        if (userId != -1L) {
            Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Ошибка регистрации", Toast.LENGTH_SHORT).show()
        }
    }
}