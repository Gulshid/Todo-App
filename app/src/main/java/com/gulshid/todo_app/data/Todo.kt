package com.gulshid.todo_app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity — one row = one todo item.
 * priority: 0=Low, 1=Medium, 2=High
 */
@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val priority: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val category: String = "Personal"
)