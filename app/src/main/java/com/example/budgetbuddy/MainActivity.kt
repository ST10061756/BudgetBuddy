package com.example.budgetbuddy

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.budgetbuddy.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import android.util.Log

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase
    private lateinit var expenseAdapter: ExpenseAdapter
    private var username: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        username = intent.getStringExtra("username")
        Log.d("MAIN", "Received username: $username")
        if (username == null) {
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
            return
        }

        setupToolbar()
        setupDatabase()
        setupRecyclerView()
        setupClickListeners()
        loadExpenses()
        updateTotalExpenses()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Welcome, $username"
            setDisplayHomeAsUpEnabled(false)
        }
    }

    private fun setupDatabase() {
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "budget_db"
        ).build()
    }

    private fun setupRecyclerView() {
        expenseAdapter = ExpenseAdapter(mutableListOf())

        binding.rvExpenses.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = expenseAdapter
        }
    }

    private fun setupClickListeners() {
        binding.fabAddExpense.setOnClickListener {
            startActivity(Intent(this, ExpenseActivity::class.java))
        }

        binding.btnViewReport.setOnClickListener {
            startActivity(Intent(this, ExpenseReportActivity::class.java))
        }

        binding.btnViewGoals.setOnClickListener {
            startActivity(Intent(this, GoalActivity::class.java))
        }
    }

    private fun loadExpenses() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                val expenses = db.expenseDao().getAllExpensesWithCategory()
                expenseAdapter.setExpenses(expenses)
                updateEmptyState(expenses.isEmpty())
            } catch (e: Exception) {
                showError("Error loading expenses: ${e.message}")
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }


    private fun updateTotalExpenses() {
        lifecycleScope.launch {
            try {
                val total = db.expenseDao().getTotalExpenses() ?: 0.0
                binding.tvTotalExpenses.text = formatCurrency(total)
            } catch (e: Exception) {
                showError("Error calculating total: ${e.message}")
            }
        }
    }


    private fun updateEmptyState(isEmpty: Boolean) {
        binding.tvNoExpenses.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvExpenses.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun formatCurrency(amount: Double): String {
        return NumberFormat.getCurrencyInstance(Locale.US).format(amount)
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                showLogoutConfirmation()
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ -> logout() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun logout() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    override fun onResume() {
        super.onResume()
        loadExpenses()
        updateTotalExpenses()
    }
}
