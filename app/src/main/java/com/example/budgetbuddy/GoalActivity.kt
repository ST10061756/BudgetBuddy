package com.example.budgetbuddy

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.budgetbuddy.R
import kotlinx.coroutines.launch

class GoalActivity : AppCompatActivity() {

    private lateinit var etMinGoalAmount: EditText
    private lateinit var etMaxGoalAmount: EditText
    private lateinit var btnSetGoal: Button
    private lateinit var tvCurrentGoal: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goal)

        etMinGoalAmount = findViewById(R.id.etMinGoalAmount)
        etMaxGoalAmount = findViewById(R.id.etMaxGoalAmount)
        btnSetGoal = findViewById(R.id.btnSetGoal)
        tvCurrentGoal = findViewById(R.id.tvCurrentGoal)

        loadCurrentGoal()

        btnSetGoal.setOnClickListener {
            val minStr = etMinGoalAmount.text.toString().trim()
            val maxStr = etMaxGoalAmount.text.toString().trim()
            val min = minStr.toDoubleOrNull()
            val max = maxStr.toDoubleOrNull()

            if (min == null || max == null || min < 0 || max <= 0 || min > max) {
                Toast.makeText(this, "Enter valid min/max values", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val dao = AppDatabase.getDatabase(this@GoalActivity).goalDao()
                dao.clearGoals()
                dao.insertGoal(Goal(minAmount = min, maxAmount = max))
                runOnUiThread {
                    Toast.makeText(this@GoalActivity, "Goal set", Toast.LENGTH_SHORT).show()
                    loadCurrentGoal()
                    etMinGoalAmount.text.clear()
                    etMaxGoalAmount.text.clear()
                }
            }
        }
    }

    private fun loadCurrentGoal() {
        lifecycleScope.launch {
            val dao = AppDatabase.getDatabase(this@GoalActivity).goalDao()
            val goal = dao.getGoal()
            runOnUiThread {
                tvCurrentGoal.text = if (goal != null) {
                    "Current Goal: Min \$${goal.minAmount}, Max \$${goal.maxAmount}"
                } else {
                    "No goal set"
                }
            }
        }
    }
}

