package com.example.budgetbuddy

import androidx.room.Embedded
import androidx.room.Relation

data class ExpenseWithCategory(
    @Embedded val expense: Expense,

    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: Category

) {
    val categoryName: String?
        get() = category.name
}