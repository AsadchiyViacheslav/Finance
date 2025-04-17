package com.example.finance

data class Transaction(
    val id: Int,
    val title: String,
    val amount: Double,
    val category: String,
    val type: String, // "income" или "expense"
    val date: String,
    val iconResId: Int
)