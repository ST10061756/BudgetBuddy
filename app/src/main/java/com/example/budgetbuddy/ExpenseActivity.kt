package com.example.budgetbuddy

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.budgetbuddy.databinding.ActivityExpenseBinding
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExpenseBinding
    private lateinit var db: AppDatabase
    private var categories = listOf<Category>()
    private var selectedCategoryId: Int? = null
    private var selectedPhotoUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedPhotoUri = uri
            Glide.with(this).load(uri).into(binding.ivPhoto)
            binding.ivPhoto.alpha = 1f
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        loadCategories()

        binding.etDate.setOnClickListener { showDatePicker() }
        binding.etStartTime.setOnClickListener { showTimePicker(isStart = true) }
        binding.etEndTime.setOnClickListener { showTimePicker(isStart = false) }

        binding.btnAddPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnSaveExpense.setOnClickListener {
            saveExpense()
        }

        // Set current date by default
        val calendar = Calendar.getInstance()
        val dateStr = "%04d-%02d-%02d".format(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        binding.etDate.setText(dateStr)
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            try {
                val categoryList = db.categoryDao().getAllCategories()
                categories = categoryList
                val names = categoryList.map { it.name }
                val adapter = ArrayAdapter(this@ExpenseActivity, android.R.layout.simple_spinner_item, names)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerCategory.adapter = adapter

                binding.spinnerCategory.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                        selectedCategoryId = categories[position].id
                    }

                    override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                        selectedCategoryId = null
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@ExpenseActivity, "Error loading categories: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val dialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val dateStr = "%04d-%02d-%02d".format(year, month + 1, dayOfMonth)
            binding.etDate.setText(dateStr)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        dialog.show()
    }

    private fun showTimePicker(isStart: Boolean) {
        val calendar = Calendar.getInstance()
        val dialog = TimePickerDialog(this, { _, hourOfDay, minute ->
            val timeStr = "%02d:%02d".format(hourOfDay, minute)
            if (isStart) binding.etStartTime.setText(timeStr) else binding.etEndTime.setText(timeStr)
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
        dialog.show()
    }

    private fun saveExpense() {
        val date = binding.etDate.text.toString()
        val startTime = binding.etStartTime.text.toString()
        val endTime = binding.etEndTime.text.toString()
        val description = binding.etDescription.text.toString()
        val amountStr = binding.etAmount.text.toString()
        val categoryId = selectedCategoryId

        if (date.isEmpty() || startTime.isEmpty() || endTime.isEmpty() || description.isEmpty() ||
            amountStr.isEmpty() || categoryId == null) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }


        val photoBytes = selectedPhotoUri?.let {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                stream.toByteArray()
            } catch (e: Exception) {
                Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show()
                null
            }
        }

        val expense = Expense(
            amount = amount,
            date = date,
            startTime = startTime,
            endTime = endTime,
            description = description,
            categoryId = categoryId,
            photo = photoBytes
        )

        lifecycleScope.launch {
            try {
                db.expenseDao().insertExpense(expense)
                runOnUiThread {
                    Toast.makeText(this@ExpenseActivity, "Expense saved", Toast.LENGTH_SHORT).show()
                    checkAndAwardBadges()
                    finish()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@ExpenseActivity, "Error saving expense: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun checkAndAwardBadges() {
        val db = AppDatabase.getDatabase(this)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val startDate = sdf.format(cal.time)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        val endDate = sdf.format(cal.time)

        lifecycleScope.launch {
            val goal = db.goalDao().getGoal()
            val totalSpent = db.expenseDao().getTotalSpentBetween(startDate, endDate) ?: 0.0
            val today = sdf.format(Date())

            // Goal Getter badge
            if (goal != null && totalSpent in goal.minAmount..goal.maxAmount) {
                if (db.badgeDao().hasBadge("Goal Getter") == 0) {
                    db.badgeDao().insertBadge(
                        Badge("Goal Getter", "You met your monthly goal!", today)
                    )
                }
            }

            // Disciplined Spender badge
            if (goal != null && totalSpent < goal.minAmount) {
                if (db.badgeDao().hasBadge("Disciplined Spender") == 0) {
                    db.badgeDao().insertBadge(
                        Badge("Disciplined Spender", "You spent less than your minimum goal!", today)
                    )
                }
            }

            // Consistent Logger badge (7 distinct days of logging)
            val streakCount = db.expenseDao().getLoggingStreak()
            if (streakCount >= 7 && db.badgeDao().hasBadge("Consistent Logger") == 0) {
                db.badgeDao().insertBadge(
                    Badge("Consistent Logger", "Logged expenses 7 days in a row!", today)
                )
            }
        }
    }

}
