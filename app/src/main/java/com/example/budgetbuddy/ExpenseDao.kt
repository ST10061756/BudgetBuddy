package com.example.budgetbuddy

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    suspend fun getAllExpenses(): List<Expense>

    @Query("SELECT SUM(amount) FROM expenses")
    suspend fun getTotalExpenses(): Double?

    @Query("""
        SELECT * FROM expenses 
        WHERE date BETWEEN strftime('%s', :startDate) * 1000 AND strftime('%s', :endDate) * 1000 
        AND (:categoryId IS NULL OR categoryId = :categoryId)
    """)
    suspend fun getExpensesFiltered(
        startDate: String,
        endDate: String,
        categoryId: Int?
    ): List<Expense>


    @Query("""
    SELECT expenses.*, categories.name AS categoryName 
    FROM expenses 
    LEFT JOIN categories ON expenses.categoryId = categories.id 
    ORDER BY date DESC
""")
    suspend fun getAllExpensesWithCategory(): List<ExpenseWithCategory>

    @Query("""
        SELECT categories.name AS categoryName, SUM(expenses.amount) AS totalAmount
        FROM expenses
        JOIN categories ON expenses.categoryId = categories.id
        WHERE expenses.date BETWEEN strftime('%s', :startDate) * 1000 AND strftime('%s', :endDate) * 1000
        GROUP BY categories.name
    """)
    suspend fun getTotalSpentByCategory(startDate: String, endDate: String): List<CategorySummary>

    @Query("""
    SELECT categories.name AS name, SUM(expenses.amount) AS total
    FROM expenses
    JOIN categories ON expenses.categoryId = categories.id
    WHERE date BETWEEN :startDate AND :endDate
    GROUP BY categories.name
""")
    suspend fun getTotalSpentPerCategory(startDate: String, endDate: String): List<CategorySpending>

    @Query("""
    SELECT SUM(amount)
    FROM expenses
    WHERE date BETWEEN :startDate AND :endDate
""")
    suspend fun getTotalSpentBetween(startDate: String, endDate: String): Double?

    @Query("""
    SELECT COUNT(DISTINCT date)
    FROM expenses
    WHERE date >= date('now', '-6 days')
""")
    suspend fun getLoggingStreak(): Int

    @Query("""
    SELECT categories.name AS categoryName, SUM(expenses.amount) AS totalAmount
    FROM expenses
    INNER JOIN categories ON expenses.categoryId = categories.id
    WHERE date BETWEEN :startDate AND :endDate
    GROUP BY expenses.categoryId
    ORDER BY totalAmount DESC
    LIMIT 1
""")
    suspend fun getTopSpendingCategory(startDate: String, endDate: String): TopCategory?

}