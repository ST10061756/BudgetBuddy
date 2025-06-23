package com.example.budgetbuddy

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class MainScreenActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_mainscreen)
            showTopCategory()

            val btnAddCategory = findViewById<Button>(R.id.btnAddCategory)
            val btnAddExpense = findViewById<Button>(R.id.btnAddExpense)
            val btnViewExpenses = findViewById<Button>(R.id.btnViewExpenses)
            val btnSetGoals = findViewById<Button>(R.id.btnSetGoals)
            val btnReports = findViewById<Button>(R.id.btnReports)
            val btnSettings = findViewById<Button>(R.id.btnSettings)
            val btnLogout = findViewById<Button>(R.id.btnLogout)
            val btnGraph = findViewById<Button>(R.id.btnGraph)
            val btnGoalProgress = findViewById<Button>(R.id.btnGoalProgress)
            val btnBadges = findViewById<Button>(R.id.btnBadges)
            val switchReminder = findViewById<Switch>(R.id.switchReminder)

            val prefs = getSharedPreferences("settings", MODE_PRIVATE)
            switchReminder.isChecked = prefs.getBoolean("reminder_enabled", false)

            switchReminder.setOnCheckedChangeListener { _, isChecked ->
                val editor = prefs.edit()
                editor.putBoolean("reminder_enabled", isChecked).apply()

                if (isChecked) scheduleDailyReminder() else cancelDailyReminder()
            }


            btnAddCategory.setOnClickListener {
                startActivity(Intent(this, CategoryActivity::class.java))
            }

            btnAddExpense.setOnClickListener {
                startActivity(Intent(this, AddExpenseActivity::class.java))
            }

            btnViewExpenses.setOnClickListener {
                startActivity(Intent(this, ExpenseReportActivity::class.java))
            }

            btnSetGoals.setOnClickListener {
                startActivity(Intent(this, GoalActivity::class.java))
            }

            btnReports.setOnClickListener {
                startActivity(Intent(this, ReportActivity::class.java))
            }

            btnGraph.setOnClickListener {
                startActivity(Intent(this, GraphActivity::class.java))
            }

            btnGoalProgress.setOnClickListener {
                startActivity(Intent(this, GoalProgressActivity::class.java))
            }

            btnBadges.setOnClickListener {
                startActivity(Intent(this, BadgeActivity::class.java))
            }

            btnSettings.setOnClickListener {
                startActivity(Intent(this, SettingsActivity::class.java))
            }

            btnLogout.setOnClickListener {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }

        }

    private fun cancelDailyReminder() {
        TODO("Not yet implemented")
    }

    private fun scheduleDailyReminder() {
        val intent = Intent(this, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 20) // 8 PM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        // If the time is in the past, set for tomorrow
        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )

        Toast.makeText(this, "Daily reminder set for 8PM", Toast.LENGTH_SHORT).show()
    }

    private fun showTopCategory() {
        val db = AppDatabase.getDatabase(this)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val startDate = sdf.format(calendar.time)

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val endDate = sdf.format(calendar.time)

        lifecycleScope.launch {
            val topCategory = db.expenseDao().getTopSpendingCategory(startDate, endDate)
            runOnUiThread {
                val textView = findViewById<TextView>(R.id.tvTopCategory)
                if (topCategory != null) {
                    textView.text = "Most spent on: ${topCategory.categoryName} — R${String.format("%.2f", topCategory.totalAmount)}"
                } else {
                    textView.text = "No expenses this month"
                }
            }
        }
    }


}