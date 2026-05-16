package com.example.projectcalendar.data.db.Entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.projectcalendar.data.db.Converter.Converters

@Entity(
    tableName = "reminders",  // Не "alarms", а "reminders"
    indices = [
        Index(value = ["event_id"]),        // Быстрый поиск по событию
        Index(value = ["is_triggered"])     // Фильтрация сработавших
    ],
    foreignKeys = [  // 🔥 СВЯЗЬ С СОБЫТИЕМ
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["event_id"],
            onDelete = ForeignKey.CASCADE  // Удалил событие → удалились напоминания
        )
    ]
)
@TypeConverters(Converters::class)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "event_id")
    val eventId: Long,  // 🔥 Ссылка на событие, к которому привязано напоминание

    @ColumnInfo(name = "offset_minutes")
    val offsetMinutes: Long,  // За сколько минут до события (-4320 = за 3 дня, -60 = за час)

    @ColumnInfo(name = "type")
    val type: String,  // "FIXED_3D", "DAY_MORNING", "DAY_EXACT", "CUSTOM"

    @ColumnInfo(name = "is_triggered")
    val isTriggered: Boolean = false,  // Системный флаг: сработало ли уведомление

    @ColumnInfo(name = "triggered_at")
    val triggeredAt: Long? = null,  // Когда именно сработало

    @ColumnInfo(name = "notification_id")
    val notificationId: Int = 0,  // ID системного уведомления Android (для отмены)

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)