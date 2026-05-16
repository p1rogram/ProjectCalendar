package com.example.projectcalendar.domain.model

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.projectcalendar.domain.model.type.ReminderType
import java.time.LocalDateTime

data class Reminder(
    val id: Long?,
    val eventId: Long,
    val offsetMinutes: Long,
    val type: ReminderType,
    val isTriggered: Boolean,
    val triggeredAt: LocalDateTime?,
    val notificationId: Long,
    val createdAt: LocalDateTime
) {

    fun getTriggeredTime(eventStart: LocalDateTime): LocalDateTime {
        return eventStart.plusMinutes(this.offsetMinutes)
    }

    fun shouldBeTriggered(eventStart: LocalDateTime): Boolean {
        if (isTriggered) return false

        val triggerTime = getTriggeredTime(eventStart)
        val now = LocalDateTime.now()

        return !triggerTime.isAfter(now)
    }

    fun markTriggered(): Reminder {
        return this.copy(
            isTriggered = true,
            triggeredAt = LocalDateTime.now()
        )
    }

    fun isValid(): Boolean {
        if (eventId <= 0) return false

        val maxMinutes = 365L * 24 * 60
        if (offsetMinutes > maxMinutes) return false

        if (type == ReminderType.CUSTOM && offsetMinutes == 0L) return false

        return true
    }
}