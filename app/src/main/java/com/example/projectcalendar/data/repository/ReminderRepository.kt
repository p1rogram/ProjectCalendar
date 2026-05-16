package com.example.projectcalendar.data.repository

import com.example.projectcalendar.data.db.Dao.ReminderDao
import com.example.projectcalendar.data.db.Entity.ReminderEntity
import com.example.projectcalendar.data.utils.toEpochMillis
import com.example.projectcalendar.data.utils.toLocalDateTime
import com.example.projectcalendar.domain.model.Reminder
import com.example.projectcalendar.domain.model.type.ReminderType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReminderRepository(
    private val reminderDao: ReminderDao // 🔒 Инкапсуляция
) {

    // ==========================================
    // 1. МАППЕРЫ
    // ==========================================
    private fun ReminderEntity.toDomain(): Reminder = Reminder(
        id = id.takeIf { it > 0 }, // ✅ Исправлена логика проверки ID
        eventId = eventId,
        offsetMinutes = offsetMinutes,
        // ✅ Безопасный парсинг Enum
        type = runCatching { ReminderType.valueOf(type) }
            .getOrDefault(ReminderType.CUSTOM),
        isTriggered = isTriggered,
        triggeredAt = triggeredAt?.toLocalDateTime(),
        notificationId = notificationId.toLong(),
        createdAt = createdAt.toLocalDateTime()
    )

    private fun Reminder.toEntity(): ReminderEntity = ReminderEntity(
        id = id ?: 0L,
        eventId = eventId,
        offsetMinutes = offsetMinutes,
        type = type.name,
        isTriggered = isTriggered,
        triggeredAt = triggeredAt?.toEpochMillis(),
        notificationId = notificationId.toInt(),
        createdAt = createdAt.toEpochMillis()
    )

    // ==========================================
    // 2. ПУБЛИЧНОЕ API
    // ==========================================

    /** Реактивный поток напоминаний для экрана события */
    fun getRemindersForEvent(eventId: Long): Flow<List<Reminder>> =
        reminderDao.getRemindersForEvent(eventId)
            .map { entities -> entities.map { it.toDomain() } }

    /** Разовый список несработавших напоминаний (для WorkManager) */
    suspend fun getPendingReminders(): List<Reminder> =
        reminderDao.getPendingReminders()
            .map { it.toDomain() }

    /** Фиксация факта отправки уведомления */
    suspend fun markAsTriggered(reminderId: Long) =
        reminderDao.markAsTriggered(reminderId)

    /** Сохранение нового напоминания (возвращает ID) */
    suspend fun addReminder(reminder: Reminder): Long =
        reminderDao.insertReminder(reminder.toEntity())

    /** Обновление параметров напоминания */
    suspend fun updateReminder(reminder: Reminder) =
        reminderDao.updateReminder(reminder.toEntity())

    /** Удаление одного напоминания */
    suspend fun deleteReminder(reminder: Reminder) =
        reminderDao.deleteReminder(reminder.toEntity())

    /** Массовое удаление при очистке события */
    suspend fun deleteRemindersByEventId(eventId: Long) =
        reminderDao.deleteRemindersByEventId(eventId)

    /** Счётчик напоминаний (для статистики) */
    suspend fun getRemindersCount(): Int =
        reminderDao.getPendingRemindersCount()
}