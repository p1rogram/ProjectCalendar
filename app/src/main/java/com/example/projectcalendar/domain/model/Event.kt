package com.example.projectcalendar.domain.model

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.projectcalendar.domain.model.type.RecurrenceType
import java.time.LocalDateTime

data class Event(
    val id: Long?,
    val title: String,
    val description: String?,
    val startDateTime: LocalDateTime,
    val isAllDay: Boolean,
    val isReminder: Boolean,
    val isImportant: Boolean,
    val recurrenceType: RecurrenceType,
    val customIntervalDays: Int,
    val recurrenceEndDate: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?
) {

    // 1. ПРОВЕРКА НА ПОВТОРЕНИЕ
    fun isRecurring(): Boolean {
        return recurrenceType != RecurrenceType.NONE &&
                !(recurrenceType == RecurrenceType.CUSTOM && customIntervalDays == 0)
    }

    fun getNextOccurrence(): LocalDateTime? {
        if (!isRecurring()) return null

        val now = LocalDateTime.now()
        var nextDate = startDateTime

        if (recurrenceEndDate != null && recurrenceEndDate.isBefore(now)) {
            return null
        }
        while (nextDate.isBefore(now)) {
            if (recurrenceEndDate != null && nextDate.isAfter(recurrenceEndDate)) {
                return null
            }

            nextDate = when (recurrenceType) {
                RecurrenceType.DAILY -> nextDate.plusDays(1)
                RecurrenceType.WEEKLY -> nextDate.plusWeeks(1)
                RecurrenceType.MONTHLY -> nextDate.plusMonths(1)
                RecurrenceType.YEARLY -> nextDate.plusYears(1)
                RecurrenceType.CUSTOM -> nextDate.plusDays(customIntervalDays.toLong())
                RecurrenceType.NONE -> break
            }
        }

        return nextDate
    }

    fun duration(): Long {
        return if (isAllDay) {
            24 * 60 * 60 * 1000
        } else {
            0L
        }
    }
    fun isValid(): Boolean {
        // 1. Заголовок не должен быть пустым
        if (title.isBlank()) return false
        // 2. Дата начала обязательна
        // УБРАНО -----------
        // 3. Дата окончания не может быть раньше начала (если она есть)
        recurrenceEndDate?.let { if (it.isBefore(startDateTime)) return false }
        // 4. Интервал повторения должен быть > 0, если тип CUSTOM
        if (recurrenceType == RecurrenceType.CUSTOM && customIntervalDays <= 0) return false
        // 5. Напоминание разовое - оно не может быть на весь день
        if (isReminder and isAllDay) return false
        return true
    }
}