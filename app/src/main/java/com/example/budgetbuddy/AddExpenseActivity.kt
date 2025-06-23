package com.example.budgetbuddy

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var etDescription: EditText
    private lateinit var etAmount: EditText
    private lateinit var tvDate: TextView
    private lateinit var tvStartTime: TextView
    private lateinit var tvEndTime: TextView
    private lateinit var rvCategories: RecyclerView
    private lateinit var btnAddExpense: Button
    private lateinit var ivPhoto: ImageView
    private lateinit var btnAddPhoto: Button

    private val categories = mutableListOf<Category>()
    private var selectedCategoryId: Int? = null

    private val calendar = Calendar.getInstance()
    private var photoUri: Uri? = null
    private var photoBitmap: Bitmap? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            photoUri = it
            ivPhoto.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        etDescription = findViewById(R.id.etDescription)
        etAmount = findViewById(R.id.etAmount)
        tvDate = findViewById(R.id.tvDate)
        tvStartTime = findViewById(R.id.tvStartTime)
        tvEndTime = findViewById(R.id.tvEndTime)
        rvCategories = findViewById(R.id.rvCategories)
        btnAddExpense = findViewById(R.id.btnAddExpense)
        ivPhoto = findViewById(R.id.ivPhoto)
        btnAddPhoto = findViewById(R.id.btnAddPhoto)

        tvDate.setOnClickListener { pickDate() }
        tvStartTime.setOnClickListener { pickTime(tvStartTime) }
        tvEndTime.setOnClickListener { pickTime(tvEndTime) }

        rvCategories.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val adapter = CategorySelectAdapter(categories) { category ->
            selectedCategoryId = category.id
            Toast.makeText(this, "Selected: ${category.name}", Toast.LENGTH_SHORT).show()
        }
        rvCategories.adapter = adapter

        loadCategories(adapter)

        btnAddPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnAddExpense.setOnClickListener {
            addExpense()
        }

        // Set current date by default
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        tvDate.text = sdf.format(calendar.time)
    }

    private fun pickDate() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, y, m, d ->
            calendar.set(y, m, d)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            tvDate.text = sdf.format(calendar.time)
        }, year, month, day).show()
    }

    private fun pickTime(targetView: TextView) {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, h, m ->
            targetView.text = String.format("%02d:%02d", h, m)
        }, hour, minute, true).show()
    }

    private fun loadCategories(adapter: CategorySelectAdapter) {
        lifecycleScope.launch {
            val dao = AppDatabase.getDatabase(this@AddExpenseActivity).categoryDao()
            categories.clear()
            categories.addAll(dao.getAllCategories())
            adapter.notifyDataSetChanged()
        }
    }

    private fun addExpense() {
        val description = etDescription.text.toString().trim()
        val amountStr = etAmount.text.toString().trim()
        val date = tvDate.text.toString()
        val startTime = tvStartTime.text.toString()
        val endTime = tvEndTime.text.toString()
        val categoryId = selectedCategoryId

        if (description.isEmpty()) {
            Toast.makeText(this, "Description cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Amount cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        if (categoryId == null) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }
        if (startTime.isEmpty() || endTime.isEmpty()) {
            Toast.makeText(this, "Please select start and end times", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
            return
        }

        // Convert photoUri to byte array if needed
        val photoBytes = photoUri?.let {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            stream.toByteArray()
        }

        val expense = Expense(
            description = description,
            amount = amount,
            date = date,
            startTime = startTime,
            endTime = endTime,
            categoryId = categoryId,
            photo = photoBytes
        )

        lifecycleScope.launch {
            val dao = AppDatabase.getDatabase(this@AddExpenseActivity).expenseDao()
            dao.insertExpense(expense)
            runOnUiThread {
                Toast.makeText(this@AddExpenseActivity, "Expense added", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
