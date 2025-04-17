package com.example.finance

data class Category(
    val id: Int,
    val name: String,
    val type: String, // "income" или "expense"
    val iconResId: Int
)