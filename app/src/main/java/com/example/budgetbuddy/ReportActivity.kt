package com.example.budgetbuddy

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ReportActivity : AppCompatActivity() {

    private lateinit var tvStartDate: TextView
    private lateinit var tvEndDate: TextView
    private lateinit var spinnerCategory: Spinner
    private lateinit var btnFilter: Button
    private lateinit var rvReport: RecyclerView
    private val expenses = mutableListOf<Expense>()
    private val categories = mutableListOf<Category>()

    private val calendarStart = Calendar.getInstance()
    private val calendarEnd = Calendar.getInstance()

    private lateinit var adapter: ExpenseReportAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        tvStartDate = findViewById(R.id.tvStartDate)
        tvEndDate = findViewById(R.id.tvEndDate)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        btnFilter = findViewById(R.id.btnFilter)
        rvReport = findViewById(R.id.rvReport)

        adapter = ExpenseReportAdapter(expenses)
        rvReport.layoutManager = LinearLayoutManager(this)
        rvReport.adapter = adapter

        tvStartDate.setOnClickListener { pickDate(calendarStart, tvStartDate) }
        tvEndDate.setOnClickListener { pickDate(calendarEnd, tvEndDate) }

        loadCategories()

        btnFilter.setOnClickListener {
            filterExpenses()
        }

        // Initialize dates
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        tvStartDate.text = sdf.format(calendarStart.time)
        tvEndDate.text = sdf.format(calendarEnd.time)
    }

    private fun pickDate(calendar: Calendar, targetView: TextView) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, y, m, d ->
            calendar.set(y, m, d)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            targetView.text = sdf.format(calendar.time)
        }, year, month, day).show()
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            val dao = AppDatabase.getDatabase(this@ReportActivity).categoryDao()
            categories.clear()
            categories.add(Category(0, "All Categories"))
            categories.addAll(dao.getAllCategories())
            runOnUiThread {
                val names = categories.map { it.name }
                val adapter = ArrayAdapter(this@ReportActivity, android.R.layout.simple_spinner_item, names)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCategory.adapter = adapter
            }
        }
    }

    private fun filterExpenses() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startDateStr = tvStartDate.text.toString()
        val endDateStr = tvEndDate.text.toString()

        lifecycleScope.launch {
            val dao = AppDatabase.getDatabase(this@ReportActivity).expenseDao()
            val result = dao.getTotalSpentByCategory(startDateStr, endDateStr)

            runOnUiThread {
                adapter.notifyDataSetChanged()
                Toast.makeText(this@ReportActivity, "${result.size} categories found", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
