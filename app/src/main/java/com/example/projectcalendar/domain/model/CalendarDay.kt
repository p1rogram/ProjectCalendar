package com.example.projectcalendar.domain.model

import java.time.LocalDate

data class CalendarDay(
    val date : LocalDate,
    val events : List<Event>,
    val tasks : List<Task>,
    val notes : List<Note>
) {
    val hasImportantEvent : Boolean = events.any { it.isImportant}
    val hasRecurringEvents : Boolean = events.any { it.isRecurring()}
    val tasksTotal : Int = tasks.size
    val tasksCompleted : Int = tasks.count {it.isCompleted}
    val taskProgress : Float = if (tasksTotal == 0) 0f else tasksCompleted.toFloat() / tasksTotal
    val hasEvents : Boolean = events.isNotEmpty()
    val hasTasks : Boolean = tasks.isNotEmpty()
    val hasNotes : Boolean = notes.isNotEmpty()
    val isEmpty : Boolean = (!hasEvents && !hasTasks && !hasNotes)
    val isToday : Boolean = date == LocalDate.now()
    val isPast : Boolean = date.isBefore(LocalDate.now())
}