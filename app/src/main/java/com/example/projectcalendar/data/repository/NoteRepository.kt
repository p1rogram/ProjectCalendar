package com.example.projectcalendar.data.repository

import com.example.projectcalendar.data.db.Dao.NoteDao
import com.example.projectcalendar.data.db.Entity.NoteEntity
import com.example.projectcalendar.data.utils.toEpochMillis
import com.example.projectcalendar.data.utils.toLocalDate
import com.example.projectcalendar.data.utils.toLocalDateTime
import com.example.projectcalendar.data.utils.toNextDayStartMillis
import com.example.projectcalendar.data.utils.toStartOfDayMillis
import com.example.projectcalendar.domain.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class NoteRepository(
    private val noteDao: NoteDao
) {

    // ==========================================
    // 1. МАППЕРЫ (Entity ↔ Domain)
    // ==========================================
    private fun NoteEntity.toDomain(): Note = Note(
        id = id.takeIf { it > 0 },
        date = date.toLocalDate(),
        content = content,
        createdAt = createdAt.toLocalDateTime(),
        updatedAt = updatedAt?.toLocalDateTime()
    )

    private fun Note.toEntity(): NoteEntity = NoteEntity(
        id = id ?: 0L,
        date = date.toStartOfDayMillis(),
        content = content,
        createdAt = createdAt.toEpochMillis(),
        updatedAt = updatedAt?.toEpochMillis()
    )

    // ==========================================
    // 2. ПУБЛИЧНОЕ API
    // ==========================================

    /** Поток заметок на конкретный день */
    fun getNotesForDate(date: LocalDate): Flow<List<Note>> =
        noteDao.getNotesByDate(date.toStartOfDayMillis())
            .map { entities -> entities.map { it.toDomain() } }

    /** Поток всех заметок (для экрана «Все заметки») */
    fun getAllNotes(): Flow<List<Note>> =
        noteDao.getAllNotes()
            .map { entities -> entities.map { it.toDomain() } }

    suspend fun getNotesForDateRange(start: LocalDate, end: LocalDate): Flow<List<Note>> =
        noteDao.getNotesByDateRange(start.toStartOfDayMillis(), end.toStartOfDayMillis())
            .map { entities -> entities.map { it.toDomain() } }
    /** Разовое получение заметки по ID (для экрана редактирования) */
    suspend fun getNoteById(id: Long): Note? =
        noteDao.getNoteById(id)?.toDomain()
    suspend fun getNotesForDateRangeOnce(start: LocalDate, end: LocalDate): List<Note> =
        noteDao.getNotesByDateRangeOnce(
            start.toStartOfDayMillis(),
            end.toNextDayStartMillis()
        ).map { it.toDomain() }

    /** Сохранить новую заметку */
    suspend fun addNote(note: Note): Long =
        noteDao.insertNote(note.toEntity())

    /** Обновить заметку (возвращает Unit, так как нам не важен результат) */
    suspend fun updateNote(note: Note) =
        noteDao.updateNote(note.toEntity())

    /** Удалить заметку */
    suspend fun deleteNote(note: Note) =
        noteDao.deleteNote(note.toEntity())

    /** Получить общее количество заметок (для статистики/бейджа) */
    suspend fun getNotesCount(): Int =
        noteDao.getNotesCount()
}