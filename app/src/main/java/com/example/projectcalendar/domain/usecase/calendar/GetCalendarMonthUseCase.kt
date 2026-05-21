package com.example.projectcalendar.domain.usecase.calendar

import com.example.projectcalendar.data.repository.EventRepository
import com.example.projectcalendar.data.repository.NoteRepository
import com.example.projectcalendar.data.repository.TaskRepository
import com.example.projectcalendar.domain.model.CalendarDay
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import java.time.YearMonth

class GetCalendarMonthUseCase(
    private val eventRepository: EventRepository,
    private val taskRepository: TaskRepository,
    private val noteRepository: NoteRepository,
) {

    suspend operator fun invoke(yearMonth: YearMonth): List<CalendarDay> = coroutineScope {
        val start = yearMonth.atDay(1)
        val end = yearMonth.atEndOfMonth()

        // 1. Параллельная загрузка данных
        // Добавляем .first(), чтобы превратить Flow<List<T>> в List<T>
        val eventsDeferred = async {
            eventRepository.getEventsForDateRange(start, end).first()
        }
        val tasksDeferred = async {
            taskRepository.getTasksForDateRange(start, end).first()
        }
        val notesDeferred = async {
            noteRepository.getNotesForDateRange(start, end).first()
        }
        // Ждём результаты
        val allEvents = eventsDeferred.await()
        val allTasks = tasksDeferred.await()
        val allNotes = notesDeferred.await()

        // 2. Сборка дней месяца
        (0 until end.dayOfMonth).map { dayOffset ->
            val currentDate = start.plusDays(dayOffset.toLong())

            // 3. Фильтрация по дате
            // Event: сравниваем дату события (извлекая LocalDate из LocalDateTime) с текущим днём
            val eventsForDay = allEvents.filter { it.startDateTime.toLocalDate() == currentDate }

            // Task/Note: сравниваем LocalDate напрямую
            val tasksForDay = allTasks.filter { it.date == currentDate }
            val notesForDay = allNotes.filter { it.date == currentDate }

            CalendarDay(
                date = currentDate,
                events = eventsForDay,
                tasks = tasksForDay,
                notes = notesForDay
            )
        }
    }
}