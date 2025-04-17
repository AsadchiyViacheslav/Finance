package com.example.finance

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finance.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var bottomNavView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        bottomNavView = binding.bottomNavigationView
        bottomNavView.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_home -> {
                    true
                }
                R.id.nav_add -> {
                    startActivity(Intent(this, AddTransactionActivity::class.java))
                    true
                }
                R.id.nav_stats -> {
                    startActivity(Intent(this, StatisticsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        setupRecyclerView()

        updateUI()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(mutableListOf())
        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = transactionAdapter
            addItemDecoration(DividerItemDecoration(this@MainActivity, DividerItemDecoration.VERTICAL))
        }
    }

    private fun updateUI() {
        val calendar = Calendar.getInstance()
        //val dateFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        //val currentMonth = dateFormat.format(calendar.time)
        val currentYear = calendar.get(Calendar.YEAR).toString()

        //binding.tvMonth.text = currentMonth
        binding.tvYear.text = currentYear

        val transactions = dbHelper.getTransactions()
        transactionAdapter.updateTransactions(transactions)

        calculateTotals(transactions)
    }

    private fun calculateTotals(transactions: List<Transaction>) {
        var totalIncome = 0.0
        var totalExpense = 0.0

        transactions.forEach { transaction ->
            when (transaction.type) {
                "income" -> totalIncome += transaction.amount
                "expense" -> totalExpense += transaction.amount
            }
        }

        binding.tvIncome.text = String.format("%.2f", totalIncome)
        binding.tvExpenses.text = String.format("%.2f", totalExpense)
        binding.tvBalance.text = String.format("%.2f", totalIncome - totalExpense)
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }
}