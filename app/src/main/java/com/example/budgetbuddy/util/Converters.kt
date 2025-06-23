package com.example.budgetbuddy.util

import androidx.room.TypeConverter
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromString(value: String?): ByteArray? {
        return value?.toByteArray()
    }

    @TypeConverter
    fun toString(bytes: ByteArray?): String? {
        return bytes?.toString(Charsets.UTF_8)
    }
} 