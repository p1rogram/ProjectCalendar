package com.example.projectcalendar.data.db.Converter

import androidx.room.TypeConverter
import com.example.projectcalendar.domain.model.type.Priority
import com.example.projectcalendar.domain.model.type.RecurrenceType

class Converters {
    // 🔁 Enum ↔ String для RecurrenceType (события)
    @TypeConverter
    fun fromRecurrenceType(value: String): RecurrenceType {
        return RecurrenceType.valueOf(value)
    }

    @TypeConverter
    fun toRecurrenceType(type: RecurrenceType): String {
        return type.name
    }

    // 🔁 Enum ↔ String для Priority (задачи)
    @TypeConverter
    fun fromPriority(value: String): Priority {
        return Priority.valueOf(value)
    }

    @TypeConverter
    fun toPriority(priority: Priority): String {
        return priority.name
    }

    // 🔁 Long ↔ LocalDateTime (если будешь использовать)
    // Пока не нужно, если хранишь даты как Long
}