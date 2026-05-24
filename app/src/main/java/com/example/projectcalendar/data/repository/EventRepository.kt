package com.example.projectcalendar.data.repository

import com.example.projectcalendar.data.db.Dao.EventDao
import com.example.projectcalendar.data.db.Entity.EventEntity
import com.example.projectcalendar.data.utils.toEpochMillis
import com.example.projectcalendar.data.utils.toLocalDateTime
import com.example.projectcalendar.data.utils.toNextDayStartMillis
import com.example.projectcalendar.data.utils.toStartOfDayMillis
import com.example.projectcalendar.domain.model.Event
import com.example.projectcalendar.domain.model.type.RecurrenceType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class EventRepository(
    private val eventDao: EventDao
) {

    // ==========================================
    // 1. МАППЕРЫ (Entity ↔ Domain)
    // ==========================================
    private fun EventEntity.toDomain(): Event = Event(
        id = id.takeIf { it > 0 },
        title = title,
        description = description.takeIf { it.isNotBlank() },
        startDateTime = startDateTime.toLocalDateTime(),
        isAllDay = isAllDay,
        isReminder = isReminder,
        isImportant = isImportant,
        recurrenceType = runCatching { RecurrenceType.valueOf(recurrenceType) }
            .getOrDefault(RecurrenceType.NONE),
        customIntervalDays = customIntervalDays,
        recurrenceEndDate = recurrenceEndDate?.toLocalDateTime(),
        createdAt = createdAt.toLocalDateTime(),
        updatedAt = updatedAt?.toLocalDateTime()
    )

    private fun Event.toEntity(): EventEntity = EventEntity(
        id = id ?: 0L,
        title = title,
        description = description ?: "",
        startDateTime = startDateTime.toEpochMillis(),
        isAllDay = isAllDay,
        isReminder = isReminder,
        isImportant = isImportant,
        recurrenceType = recurrenceType.name,
        customIntervalDays = customIntervalDays,
        recurrenceEndDate = recurrenceEndDate?.toEpochMillis(),
        createdAt = createdAt.toEpochMillis(),
        updatedAt = updatedAt?.toEpochMillis()
    )

    /** Поток событий за период (для календаря) */
    fun getEventsForDateRange(start: LocalDate, end: LocalDate): Flow<List<Event>> =
        eventDao.getEventsForDateRange(
            start.toStartOfDayMillis(),
            end.toNextDayStartMillis()
        ).map { entities -> entities.map { it.toDomain() } }
    suspend fun getEventsForDateRangeOnce(start: LocalDate, end: LocalDate): List<Event> =
        eventDao.getEventsForDateRangeOnce(
            start.toStartOfDayMillis(),
            end.toNextDayStartMillis()
        ).map { it.toDomain() }

    /** Разовое получение события по ID */
    suspend fun getEventById(id: Long): Event? =
        eventDao.getEventById(id)?.toDomain()

    /** Сохранение нового события */
    suspend fun addEvent(event: Event): Long =
        eventDao.insertEvent(event.toEntity())

    /** Обновление существующего события */
    suspend fun updateEvent(event: Event) =
        eventDao.updateEvent(event.toEntity())

    /** Удаление события (требует наличия метода в DAO) */
    suspend fun deleteEvent(event: Event) =
        eventDao.deleteEvent(event.toEntity())
}