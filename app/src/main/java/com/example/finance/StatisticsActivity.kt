package com.example.finance

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Insets.add
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.finance.databinding.ActivityStatisticsBinding
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StatisticsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStatisticsBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        setupDateDisplay()
        loadStatistics()
    }

    private fun setupDateDisplay() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val dateParts = dateFormat.format(calendar.time).split(" ")

        binding.tvStatMonth.text = dateParts[0].replaceFirstChar { it.uppercase() }
        binding.tvStatYear.text = dateParts[1]
    }


    private fun loadStatistics() {
        val transactions = dbHelper.getTransactions()
        val (income, expenses) = calculateTotals(transactions)

        setupPieChart(income, expenses)
        setupCategoryStats(income, expenses)
    }

    private fun calculateTotals(transactions: List<Transaction>): Pair<Double, Double> {
        var income = 0.0
        var expenses = 0.0

        transactions.forEach { transaction ->
            when (transaction.type) {
                "income" -> income += transaction.amount
                "expense" -> expenses += transaction.amount
            }
        }

        return Pair(income, expenses)
    }

    private fun setupPieChart(income: Double, expenses: Double) {
        val entries = ArrayList<PieEntry>().apply {
            add(PieEntry(income.toFloat(), "Доходы"))
            add(PieEntry(expenses.toFloat(), "Расходы"))
        }

        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                ContextCompat.getColor(this@StatisticsActivity, R.color.income),
                ContextCompat.getColor(this@StatisticsActivity, R.color.expense)
            )
            valueTextColor = Color.WHITE
            valueTextSize = 12f
        }

        binding.pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            setEntryLabelColor(Color.BLACK)
            animateY(1000, Easing.EaseInOutQuad)
            invalidate()
        }
    }

    private fun setupCategoryStats(income: Double, expenses: Double) {
        binding.llIncomeCategories.removeAllViews()
        binding.llExpenseCategories.removeAllViews()

        val incomeCategories = calculateCategoryTotals("income")
        val expenseCategories = calculateCategoryTotals("expense")

        incomeCategories.forEach { (category, amount) ->
            addCategoryView(
                binding.llIncomeCategories,
                category,
                amount,
                income,
                R.color.income
            )
        }

        // Добавляем расходы
        expenseCategories.forEach { (category, amount) ->
            addCategoryView(
                binding.llExpenseCategories,
                category,
                amount,
                expenses,
                R.color.expense
            )
        }
    }

    private fun calculateCategoryTotals(type: String): Map<String, Double> {
        val transactions = dbHelper.getTransactions()
        return transactions
            .filter { it.type == type }
            .groupingBy { it.category }
            .fold(0.0) { sum, transaction -> sum + transaction.amount }
    }

    private fun addCategoryView(
        container: LinearLayout,
        categoryName: String,
        amount: Double,
        total: Double,
        colorRes: Int
    ) {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.item_category_stat, container, false)

        val tvCategory = view.findViewById<TextView>(R.id.tvCategory)
        val tvAmount = view.findViewById<TextView>(R.id.tvAmount)
        val tvPercentage = view.findViewById<TextView>(R.id.tvPercentage)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        val percentage = if (total > 0) (amount / total * 100).toInt() else 0
        val color = ContextCompat.getColor(this, colorRes)

        tvCategory.text = categoryName
        tvAmount.text = String.format(Locale.getDefault(), "%.2f", amount)
        tvPercentage.text = "$percentage%"
        tvAmount.setTextColor(color)

        progressBar.progress = percentage
        progressBar.progressTintList = ColorStateList.valueOf(color)

        container.addView(view)
    }
}