package com.example.projectcalendar.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import java.time.LocalDate

@Immutable  // ✅ Строже чем @Stable: гарантирует что ВСЕ свойства final и стабильны
data class CalendarDay(
    val date: LocalDate,
    val events: ImmutableList<Event> = persistentListOf(),
    val tasks: ImmutableList<Task> = persistentListOf(),
    val notes: ImmutableList<Note> = persistentListOf(),
) {
    val hasImportantEvent: Boolean = events.any { it.isImportant }
    val hasUnimportantEvent: Boolean = events.any { !it.isImportant }
    val hasAnyContent: Boolean = events.isNotEmpty() || tasks.isNotEmpty() || notes.isNotEmpty()
    val hasRecurringEvents: Boolean = events.any { it.isRecurring() }
    val tasksTotal: Int = tasks.size
    val tasksCompleted: Int = tasks.count { it.isCompleted }
    val taskProgress: Float = if (tasksTotal == 0) 0f else tasksCompleted.toFloat() / tasksTotal
    val hasEvents: Boolean get() = events.isNotEmpty()
    val hasTasks: Boolean get() = tasks.isNotEmpty()
    val hasTasksOrNotes: Boolean get() = tasks.isNotEmpty() || notes.isNotEmpty()
    val hasNotes: Boolean get() = notes.isNotEmpty()
    val isEmpty: Boolean get() = !hasAnyContent
    val isToday: Boolean = date == LocalDate.now()
    val isPast: Boolean = date.isBefore(LocalDate.now())
}