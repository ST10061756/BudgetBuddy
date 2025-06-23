package com.example.budgetbuddy

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


import kotlinx.coroutines.launch

class CategoryActivity : AppCompatActivity() {

    private lateinit var etCategoryName: EditText
    private lateinit var btnAddCategory: Button
    private lateinit var rvCategories: RecyclerView
    private lateinit var adapter: CategoryAdapter
    private var categories = mutableListOf<Category>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        etCategoryName = findViewById(R.id.etCategoryName)
        btnAddCategory = findViewById(R.id.btnAddCategory)
        rvCategories = findViewById(R.id.rvCategories)

        adapter = CategoryAdapter(categories) { category ->
            deleteCategory(category)
        }
        rvCategories.layoutManager = LinearLayoutManager(this)
        rvCategories.adapter = adapter

        loadCategories()

        btnAddCategory.setOnClickListener {
            val name = etCategoryName.text.toString().trim()
            if (name.isNotEmpty()) {
                addCategory(name)
            } else {
                Toast.makeText(this, "Category name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            val dao = AppDatabase.getDatabase(this@CategoryActivity).categoryDao()
            categories.clear()
            categories.addAll(dao.getAllCategories())
            adapter.notifyDataSetChanged()
        }
    }

    private fun addCategory(name: String) {
        lifecycleScope.launch {
            val dao = AppDatabase.getDatabase(this@CategoryActivity).categoryDao()
            val newCategory = Category(name = name)
            dao.insertCategory(newCategory)
            etCategoryName.text.clear()
            loadCategories()
            Toast.makeText(this@CategoryActivity, "Category added", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteCategory(category: Category) {
        lifecycleScope.launch {
            val dao = AppDatabase.getDatabase(this@CategoryActivity).categoryDao()
            dao.deleteCategory(category)
            loadCategories()
            Toast.makeText(this@CategoryActivity, "Category deleted", Toast.LENGTH_SHORT).show()
        }
    }
}
