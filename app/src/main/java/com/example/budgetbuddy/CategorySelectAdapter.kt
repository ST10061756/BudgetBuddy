package com.example.budgetbuddy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CategorySelectAdapter(
    private val categories: List<Category>,
    private val onCategorySelected: (Category) -> Unit
) : RecyclerView.Adapter<CategorySelectAdapter.CategorySelectViewHolder>() {

    private var selectedPosition = -1

    inner class CategorySelectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategorySelectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_select, parent, false)
        return CategorySelectViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategorySelectViewHolder, position: Int) {
        val category = categories[position]
        holder.tvCategoryName.text = category.name
        holder.itemView.isSelected = (selectedPosition == position)
        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
            onCategorySelected(category)
        }
    }

    override fun getItemCount(): Int = categories.size
}
