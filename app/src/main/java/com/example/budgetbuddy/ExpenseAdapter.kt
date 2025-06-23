package com.example.budgetbuddy

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbuddy.databinding.ItemExpenseBinding
import java.text.NumberFormat
import java.util.*

class ExpenseAdapter(private val expenses: MutableList<ExpenseWithCategory>) :
    RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {


    inner class ExpenseViewHolder(private val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(expenseWithCategory: ExpenseWithCategory) {
            val expense = expenseWithCategory.expense
            val category = expenseWithCategory.category

            binding.tvDescription.text = expense.description
            binding.tvAmount.text = formatCurrency(expense.amount)
            binding.tvCategory.text = category.name
            binding.tvDate.text = expense.date

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(expenses[position])
    }

    override fun getItemCount(): Int = expenses.size

    fun setExpenses(newExpenses: List<ExpenseWithCategory>) {
        expenses.clear()
        expenses.addAll(newExpenses)
        notifyDataSetChanged()
    }

    private fun formatCurrency(amount: Double): String {
        return NumberFormat.getCurrencyInstance(Locale.getDefault()).format(amount)
    }
}
