package com.example.finance

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finance.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {

        val sharedPref = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val savedUsername = sharedPref.getString("username", null)

        // Если логин уже сохранён — переходим на главный экран
        if (savedUsername != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelper = DatabaseHelper(this)

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString()

            if (validateInput(username, password)) {
                loginUser(username, password)
            }
        }
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateInput(username: String, password: String): Boolean {
        return when {
            username.isEmpty() -> {
                binding.etUsername.error = "Введите логин"
                false
            }
            password.isEmpty() -> {
                binding.etPassword.error = "Введите пароль"
                false
            }
            else -> true
        }
    }

    private fun loginUser(username: String, password: String) {
        if (dbHelper.loginUser(username, password)) {
            Toast.makeText(this, "Вход выполнен", Toast.LENGTH_SHORT).show()
            val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("username", username)
                apply()
            }
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show()
        }
    }
}