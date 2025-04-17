package com.example.finance

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class CategoryAdapter(
    private val context: Context,
    private var categories: MutableList<Category>,
    private val onCategorySelected: (Category) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = categories.size

    override fun getItem(position: Int): Category = categories[position]

    override fun getItemId(position: Int): Long = categories[position].id.toLong()

    private var selectedPosition = -1

    fun updateCategories(newCategories: List<Category>) {
        categories.clear()
        categories.addAll(newCategories)
        selectedPosition = -1 // Сбрасываем выбор при обновлении списка
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_category, parent, false)

        val category = categories[position]
        val ivIcon = view.findViewById<ImageView>(R.id.ivIcon)
        val tvName = view.findViewById<TextView>(R.id.tvName)
        val selectionIndicator = view.findViewById<View>(R.id.selectionIndicator)

        // Устанавливаем правильную иконку из объекта Category
        ivIcon.setImageResource(category.iconResId)
        tvName.text = category.name
        selectionIndicator.visibility = if (position == selectedPosition) View.VISIBLE else View.INVISIBLE

        view.setOnClickListener {
            selectedPosition = position
            notifyDataSetChanged()
            onCategorySelected(category) // Передаем весь объект категории
        }

        return view
    }
}