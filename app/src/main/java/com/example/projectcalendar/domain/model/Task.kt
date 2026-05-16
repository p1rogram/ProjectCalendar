package com.example.projectcalendar.domain.model

import com.example.projectcalendar.domain.model.type.Priority
import java.time.LocalDate
import java.time.LocalDateTime

data class Task(
    val id: Long?,
    val title: String,
    val description: String?,
    val date: LocalDate,
    val isCompleted: Boolean,
    val completedAt: LocalDateTime?,
    val priority: Priority,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?
) {
    fun markComplete(): Task {
        return this.copy(
            isCompleted = true,
            completedAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    fun markIncomplete(): Task {
        return this.copy(
            isCompleted = false,
            completedAt = null,
            updatedAt = LocalDateTime.now()
        )
    }

    fun isOverdue(): Boolean {
        if (isCompleted) return false
        return date.isBefore(LocalDate.now())
    }

    fun getPriorityWeight(): Int {
        return when (priority) {
            Priority.HIGH -> 3
            Priority.MEDIUM -> 2
            Priority.LOW -> 1
        }
    }

    fun isValid(): Boolean {
        return title.isNotBlank()
    }
}