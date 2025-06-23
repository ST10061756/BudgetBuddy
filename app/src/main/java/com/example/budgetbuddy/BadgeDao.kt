package com.example.budgetbuddy

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BadgeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadge(badge: Badge)

    @Query("SELECT * FROM badges")
    suspend fun getAllBadges(): List<Badge>

    @Query("SELECT COUNT(*) FROM badges WHERE name = :badgeName")
    suspend fun hasBadge(badgeName: String): Int
}