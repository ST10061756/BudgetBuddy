package com.example.budgetbuddy

import android.graphics.Color
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class GoalProgressActivity : AppCompatActivity() {

    private lateinit var tvTotalSpent: TextView
    private lateinit var tvGoalRange: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgressFeedback: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goal_progress)

        tvTotalSpent = findViewById(R.id.tvTotalSpent)
        tvGoalRange = findViewById(R.id.tvGoalRange)
        progressBar = findViewById(R.id.progressBarGoal)
        tvProgressFeedback = findViewById(R.id.tvProgressFeedback)

        loadGoalProgress()
    }

    private fun loadGoalProgress() {
        val db = AppDatabase.getDatabase(this)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Get first and last day of current month
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val startDate = sdf.format(calendar.time)
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val endDate = sdf.format(calendar.time)

        lifecycleScope.launch {
            try {
                val goal = db.goalDao().getGoal()
                val totalSpent = db.expenseDao().getTotalSpentBetween(startDate, endDate) ?: 0.0

                runOnUiThread {
                    tvTotalSpent.text = "This Month's Spending: $%.2f".format(totalSpent)

                    if (goal != null) {
                        tvGoalRange.text = "Goal Range: $%.2f - $%.2f".format(goal.minAmount, goal.maxAmount)

                        val progressPercent = ((totalSpent / goal.maxAmount) * 100).coerceAtMost(100.0)
                        progressBar.progress = progressPercent.toInt()

                        when {
                            totalSpent < goal.minAmount -> {
                                tvProgressFeedback.text = "You're spending below your goal 🟡"
                                tvProgressFeedback.setTextColor(Color.parseColor("#FF9800"))
                            }
                            totalSpent in goal.minAmount..goal.maxAmount -> {
                                tvProgressFeedback.text = "You're on track! ✅"
                                tvProgressFeedback.setTextColor(Color.parseColor("#4CAF50"))
                            }
                            else -> {
                                tvProgressFeedback.text = "You've exceeded your goal ❗"
                                tvProgressFeedback.setTextColor(Color.parseColor("#F44336"))
                            }
                        }
                    } else {
                        tvGoalRange.text = "No goal set"
                        tvProgressFeedback.text = "Set a spending goal to track your progress"
                    }
                }

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@GoalProgressActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
