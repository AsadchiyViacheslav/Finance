package com.example.finance

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finance.databinding.ActivityAddCategoryBinding

class AddCategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddCategoryBinding
    private lateinit var dbHelper: DatabaseHelper
    private var selectedIconResId = R.drawable.ic_other // Иконка по умолчанию
    private var categoryType = "expense"

    // Список доступных иконок
    private val iconResIds = listOf(
        R.drawable.ic_food,
        R.drawable.ic_cafe,
        R.drawable.ic_transport,
        R.drawable.ic_entertainment,
        R.drawable.ic_salary,
        R.drawable.ic_gift,
        R.drawable.ic_bank,
        R.drawable.ic_hospital,
        R.drawable.ic_internet,
        R.drawable.ic_music,
        R.drawable.ic_game,
        R.drawable.ic_car,
        R.drawable.ic_phone,
        R.drawable.ic_book,
        R.drawable.ic_cash,
        R.drawable.ic_other
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        setupTypeSelector()
        setupIconGrid()
        setupAddButton()
    }

    private fun setupTypeSelector() {
        intent.getStringExtra("category_type")?.let {
            categoryType = it
            binding.rgType.check(
                if (it == "income") R.id.rbIncome else R.id.rbExpense
            )
        }

        binding.rgType.setOnCheckedChangeListener { _, checkedId ->
            categoryType = if (checkedId == R.id.rbIncome) "income" else "expense"
        }
    }

    private fun setupIconGrid() {
        binding.gvIcons.adapter = IconAdapter(this, iconResIds) { resId ->
            selectedIconResId = resId
            binding.ivSelectedIcon.setImageResource(resId) // Показываем выбранную иконку
        }
    }

    private fun setupAddButton() {
        binding.btnAddCategory.setOnClickListener {
            if (validateInput()) {
                addCategoryToDatabase()
            }
        }
    }

    private fun validateInput(): Boolean {
        val name = binding.etCategoryName.text.toString().trim()

        return when {
            name.isEmpty() -> {
                binding.etCategoryName.error = "Введите название"
                false
            }
            name.length > 20 -> {
                binding.etCategoryName.error = "Максимум 20 символов"
                false
            }
            dbHelper.isCategoryExists(name, categoryType) -> {
                binding.etCategoryName.error = "Категория уже существует"
                false
            }
            selectedIconResId == 0 -> {
                Toast.makeText(this, "Выберите иконку", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun addCategoryToDatabase() {
        val name = binding.etCategoryName.text.toString().trim()

        val category = Category(
            id = 0, // БД сама сгенерирует ID
            name = name,
            type = categoryType,
            iconResId = selectedIconResId
        )

        val newId = dbHelper.addCategory(category)
        if (newId != -1L) {
            setResult(RESULT_OK, Intent().apply {
                putExtra("category_id", newId)
                putExtra("category_name", name)
                putExtra("category_type", categoryType)
                putExtra("category_icon", selectedIconResId)
            })
            finish()
        } else {
            Toast.makeText(this, "Ошибка сохранения", Toast.LENGTH_SHORT).show()
        }
    }
}