package com.example.finance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(
    private var transactions: MutableList<Transaction>,
    private val onItemClick: (Transaction) -> Unit = {}
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivCategoryIcon: ImageView = itemView.findViewById(R.id.ivCategoryIcon)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)

        fun bind(transaction: Transaction) {
            tvTitle.text = transaction.title
            tvCategory.text = transaction.category

            // Установка цвета суммы
            val amountColor = when (transaction.type) {
                "income" -> ContextCompat.getColor(itemView.context, R.color.income)
                else -> ContextCompat.getColor(itemView.context, R.color.expense)
            }
            tvAmount.setTextColor(amountColor)

            // Форматирование суммы
            val amountText = when (transaction.type) {
                "income" -> "+${transaction.amount}"
                else -> "-${transaction.amount}"
            }
            tvAmount.text = amountText

            // Основное исправление: используем iconResId из транзакции
            ivCategoryIcon.setImageResource(
                if (transaction.iconResId != 0) {
                    transaction.iconResId
                } else {
                    // Fallback иконка, если по какой-то причине iconResId = 0
                    when (transaction.category) {
                        "Продукты" -> R.drawable.ic_food
                        "Кафе" -> R.drawable.ic_cafe
                        "Транспорт" -> R.drawable.ic_transport
                        "Зарплата" -> R.drawable.ic_salary
                        else -> R.drawable.ic_other
                    }
                }
            )

            itemView.setOnClickListener { onItemClick(transaction) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount(): Int = transactions.size

    fun updateTransactions(newTransactions: List<Transaction>) {
        transactions = newTransactions.toMutableList()
        notifyDataSetChanged()
    }
}