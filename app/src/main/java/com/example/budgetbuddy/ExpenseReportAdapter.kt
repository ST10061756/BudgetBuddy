package com.example.budgetbuddy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExpenseReportAdapter(private val expenses: List<Expense>) : RecyclerView.Adapter<ExpenseReportAdapter.ExpenseViewHolder>() { // Changed ViewHolder generic type

    // Changed to ExpenseViewHolder to match the class name
    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val description: TextView = itemView.findViewById(R.id.tvDescription)
        val amount: TextView = itemView.findViewById(R.id.tvAmount)
        val category: TextView = itemView.findViewById(R.id.tvCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val currentExpense = expenses[position] // Renamed for clarity if 'expense' is a type
        holder.description.text = currentExpense.description
        holder.amount.text = "Amount: \$${currentExpense.amount}"


        holder.category.text = "Category: ${currentExpense.categoryId}"
    }

    override fun getItemCount(): Int = expenses.size
}