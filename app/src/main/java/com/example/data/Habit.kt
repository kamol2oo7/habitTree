package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String, // "SAGE", "OCEAN", "LAVENDER", "TERRACOTTA"
    val createdAt: Long = System.currentTimeMillis(),
    val notificationTime: String? = null, // "HH:MM" format
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val iconName: String = "SPA",
    val species: String = "CHERRY_BLOSSOM"
)
