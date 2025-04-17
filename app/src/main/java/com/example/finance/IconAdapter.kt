package com.example.finance

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView

class IconAdapter(
    private val context: Context,
    private val iconResIds: List<Int>,
    private val onIconSelected: (Int) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = iconResIds.size

    override fun getItem(position: Int): Int = iconResIds[position]

    override fun getItemId(position: Int): Long = position.toLong()

    private var selectedPosition = -1

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_icon, parent, false)

        val ivIcon = view.findViewById<ImageView>(R.id.ivIcon)
        val selectionIndicator = view.findViewById<View>(R.id.selectionIndicator)

        ivIcon.setImageResource(iconResIds[position])
        selectionIndicator.visibility = if (position == selectedPosition) View.VISIBLE else View.INVISIBLE

        view.setOnClickListener {
            selectedPosition = position
            notifyDataSetChanged()
            onIconSelected(iconResIds[position])
        }

        return view
    }
}