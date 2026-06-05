package com.example.projectcalendar.domain.model

import android.icu.text.CaseMap
import java.time.LocalDate
import java.time.LocalDateTime

data class Note(
    val id: Long?,
    val date: LocalDate,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?,
    val title: String
) {
    fun isEmpty(): Boolean {
        return content.isBlank()
    }

    fun wordCount(): Int {
        if (this.content.isBlank()) return 0
        return content.trim().split(Regex("\\s+")).count { it.isNotEmpty() }
    }

    fun uploadNewContent(newContent: String): Note {
        if (newContent.isBlank()) return this
        return this.copy(
            content = newContent,
            updatedAt = LocalDateTime.now()
        )
    }


    fun isValid(): Boolean {
        return content.isNotBlank()
    }
}