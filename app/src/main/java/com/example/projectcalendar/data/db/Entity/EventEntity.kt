package com.example.projectcalendar.data.db.Entity


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.projectcalendar.data.db.Converter.Converters

@Entity(
    tableName = "events",
    indices = [
        Index(value = ["startDateTime"]),           // Индекс по дате
        Index(value = ["isImportant"]),             // Индекс по важности
        Index(value = ["recurrenceType"])           // Индекс по типу повторения
    ]
)
@TypeConverters(Converters::class)
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,                   // 0 = новое событие, Room подставит реальный ID

    val title: String,                  // Обязательное поле (без значения по умолчанию)

    val description: String = "",       // Пустая строка если не заполнили
    // ИЛИ: val description: String? = null,  // Если хочешь разрешить null

    val startDateTime: Long,            // Обязательное (дата события)

    val isAllDay: Boolean = false,      // По умолчанию — не весь день
    val isImportant: Boolean = false,   // По умолчанию — не важное

    val recurrenceType: String = "NONE", // По умолчанию — без повторения
    val customIntervalDays: Int = 0,    // По умолчанию — 0 дней
    val recurrenceEndDate: Long? = null, // По умолчанию — бесконечное повторение

    val createdAt: Long = System.currentTimeMillis(), // Ставим время создания автоматически
    val updatedAt: Long? = null         // По умолчанию не обновлялось
)
