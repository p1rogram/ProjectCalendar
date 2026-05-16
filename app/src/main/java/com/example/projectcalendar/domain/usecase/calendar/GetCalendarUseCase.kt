package com.example.projectcalendar.domain.usecase.calendar

import com.example.projectcalendar.data.repository.EventRepository
import com.example.projectcalendar.data.repository.NoteRepository
import com.example.projectcalendar.data.repository.TaskRepository
import com.example.projectcalendar.domain.model.CalendarDay
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import java.time.LocalDate


class GetCalendarUseCase(
    private val eventRepository: EventRepository,
    private val taskRepository: TaskRepository,
    private val noteRepository: NoteRepository
) {


    suspend operator fun invoke(date: LocalDate): CalendarDay = coroutineScope {
        val eventsDeferred = async {
            eventRepository.getEventsForDateRange(date, date).first()
        }

        val tasksDeferred = async {
            taskRepository.getTasksForDate(date).first()
        }

        val notesDeferred = async {
            noteRepository.getNotesForDate(date).first()
        }

        CalendarDay(
            date = date,
            events = eventsDeferred.await(),
            tasks = tasksDeferred.await(),
            notes = notesDeferred.await()
        )
    }
}