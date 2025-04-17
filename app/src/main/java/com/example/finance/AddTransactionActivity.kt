package com.example.finance

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.finance.databinding.ActivityAddTransactionBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var categoryAdapter: CategoryAdapter
    private var selectedCategory: Category? = null
    private var transactionType = "expense" // По умолчанию расход

    private val addCategoryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let { data ->
                val category = Category(
                    id = data.getIntExtra("category_id", 0),
                    name = data.getStringExtra("category_name") ?: "",
                    type = data.getStringExtra("category_type") ?: "expense",
                    iconResId = data.getIntExtra("category_icon", R.drawable.ic_other)
                )
                // Обновляем список с новой категорией
                loadCategories(category.type)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        setupViews()
        loadCategories(transactionType)
    }

    private fun setupViews() {
        // Настройка RadioGroup для выбора типа транзакции
        binding.rgType.setOnCheckedChangeListener { _, checkedId ->
            transactionType = when (checkedId) {
                R.id.rbIncome -> "income"
                else -> "expense"
            }
            loadCategories(transactionType)
        }

        // Настройка GridView для категорий
        categoryAdapter = CategoryAdapter(this, mutableListOf()) { category ->
            selectedCategory = category // Сохраняем весь объект, а не только имя
            Log.d("CategorySelect", "Selected: ${category.name} with icon: ${category.iconResId}")
        }
        binding.gvCategories.adapter = categoryAdapter

        // Обработка нажатия кнопки "Добавить транзакцию"
        binding.btnAdd.setOnClickListener {
            addTransaction()
        }

        // Обработка нажатия кнопки "Добавить категорию"
        binding.btnAddNewCategory.setOnClickListener {
            val intent = Intent(this, AddCategoryActivity::class.java).apply {
                putExtra("category_type", transactionType)
            }
            addCategoryLauncher.launch(intent)
        }
    }

    private fun loadCategories(type: String) {
        val categories = dbHelper.getCategoriesByType(type)
        categoryAdapter.updateCategories(categories)
        selectedCategory = if (categories.isNotEmpty()) categories[0] else null
    }

    private fun addTransaction() {
        val title = binding.etTitle.text.toString().trim()
        val amountText = binding.etAmount.text.toString().trim()

        when {
            title.isEmpty() -> {
                binding.etTitle.error = "Введите название"
                return
            }
            amountText.isEmpty() -> {
                binding.etAmount.error = "Введите сумму"
                return
            }
            selectedCategory == null -> {
                Toast.makeText(this, "Выберите категорию", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val amount = amountText.toDoubleOrNull() ?: run {
            binding.etAmount.error = "Некорректная сумма"
            return
        }

        val currentDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())

        val transaction = Transaction(
            id = 0, // ID будет сгенерирован базой данных
            title = title,
            amount = amount,
            category = selectedCategory!!.name,
            type = transactionType,
            date = currentDate,
            iconResId = selectedCategory!!.iconResId
        )

        if (dbHelper.addTransaction(transaction) != -1L) {
            Toast.makeText(this, "Транзакция добавлена", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        } else {
            Toast.makeText(this, "Ошибка при добавлении", Toast.LENGTH_SHORT).show()
        }
    }
}