package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val subject: String,
    val category: String = "Umum",
    val priority: String = "Sedang",
    val deadline: Long, // Timestamp in milliseconds
    val isCompleted: Boolean = false
)
