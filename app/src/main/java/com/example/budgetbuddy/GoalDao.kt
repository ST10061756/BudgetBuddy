package com.example.budgetbuddy

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal)

    @Query("DELETE FROM goals")
    suspend fun clearGoals()

    @Query("SELECT * FROM goals LIMIT 1")
    suspend fun getGoal(): Goal?
}
