package com.example.projectcalendar.presentation.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectcalendar.data.repository.EventRepository
import com.example.projectcalendar.data.repository.NoteRepository
import com.example.projectcalendar.data.repository.TaskRepository
import com.example.projectcalendar.domain.model.CalendarDay
import com.example.projectcalendar.domain.model.Event
import com.example.projectcalendar.domain.model.Note
import com.example.projectcalendar.domain.model.Task
import com.example.projectcalendar.domain.model.type.Priority
import com.example.projectcalendar.domain.model.type.RecurrenceType
import com.example.projectcalendar.domain.usecase.calendar.GetCalendarMonthUseCase
import com.example.projectcalendar.presentation.ui.common.AddMode
import com.example.projectcalendar.presentation.ui.common.CalendarPage
import com.example.projectcalendar.presentation.ui.common.CalendarUiState
import com.example.projectcalendar.presentation.ui.common.LoadStatus
import com.example.projectcalendar.presentation.ui.calendar.components.AddItemCommand
import com.example.projectcalendar.presentation.ui.mapper.CalendarGridMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val mapper: CalendarGridMapper,
    private val getCalendarMonthUseCase: GetCalendarMonthUseCase,
    private val eventRepository: EventRepository,
    private val taskRepository: TaskRepository,
    private val noteRepository: NoteRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CalendarUiState(
            status = LoadStatus.Loading,
            pages = persistentListOf(),
            initialPageIndex = 0,
            selectedDate = LocalDate.now(),
            showAddItemSheet = false,
            showDetailsSheet = false,
            currentAddMode = null
        )
    )
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private val isSaving = AtomicBoolean(false)
    private var loadMonthJob: Job? = null
    // ✅ Вспомогательный класс для возврата 4 значений
    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
    @RequiresApi(Build.VERSION_CODES.O)
    fun loadMonth(year: Int, month: Int, showLoading: Boolean = true) {
        loadMonthJob?.cancel()

        loadMonthJob = viewModelScope.launch {
            try {
                val targetMonth = YearMonth.of(year, month)

                // ✅ Проверяем кэш
                val cachedPages = _uiState.value.monthsCache[targetMonth]
                if (cachedPages != null) {
                    Log.d("CalendarViewModel", "Using cached pages for $targetMonth")
                    _uiState.update { it.copy(pages = cachedPages, status = LoadStatus.Success) }
                    preloadAdjacentMonths(targetMonth)
                    return@launch
                }

                if (_uiState.value.selectedDate.year != year || _uiState.value.selectedDate.monthValue != month) {
                    _uiState.update { it.copy(selectedDate = LocalDate.of(year, month, 1)) }
                }
                if (showLoading) {
                    _uiState.update { it.copy(status = LoadStatus.Loading) }
                }

                val firstDay = LocalDate.of(year, month, 1)
                val emptyGrid = mapper.map(firstDay)
                val monthData = getCalendarMonthUseCase.invoke(YearMonth.of(year, month)).associateBy { it.date }

                val filledGrid = emptyGrid.map { week ->
                    week.map { monthData[it?.date] ?: it }.toImmutableList()
                }.toImmutableList()

                val newPages = filledGrid
                    .chunked(4)
                    .map { weekChunk ->
                        if (weekChunk.size < 4) {
                            weekChunk + List(4 - weekChunk.size) {
                                List(7) { null as CalendarDay? }.toImmutableList()
                            }
                        } else {
                            weekChunk
                        }
                    }
                    .mapIndexed { pageIndex, column ->
                        CalendarPage(
                            yearMonth = YearMonth.of(year, month),
                            grid = column.toImmutableList(),
                            isSecondHalf = pageIndex > 0
                        )
                    }
                    .toImmutableList()

                // ✅ Сохраняем в кэш
                _uiState.update { state ->
                    val newCache = state.monthsCache.toPersistentMap().put(targetMonth, newPages)
                    state.copy(
                        pages = newPages,
                        monthsCache = newCache,
                        status = LoadStatus.Success
                    )
                }

                preloadAdjacentMonths(targetMonth)

            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _uiState.update { it.copy(status = LoadStatus.Error) }
                Log.e("CalendarViewModel", "Error loading month", e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun preloadAdjacentMonths(currentMonth: YearMonth) {
        val prevMonth = currentMonth.minusMonths(1)
        val nextMonth = currentMonth.plusMonths(1)

        if (_uiState.value.monthsCache[prevMonth] == null) {
            viewModelScope.launch {
                try {
                    val pages = loadMonthData(prevMonth)
                    _uiState.update { state ->
                        state.copy(
                            monthsCache = state.monthsCache.toPersistentMap().put(prevMonth, pages)
                        )
                    }
                    Log.d("CalendarViewModel", "Preloaded $prevMonth")
                } catch (e: Exception) {
                    Log.e("CalendarViewModel", "Error preloading $prevMonth", e)
                }
            }
        }

        if (_uiState.value.monthsCache[nextMonth] == null) {
            viewModelScope.launch {
                try {
                    val pages = loadMonthData(nextMonth)
                    _uiState.update { state ->
                        state.copy(
                            monthsCache = state.monthsCache.toPersistentMap().put(nextMonth, pages)
                        )
                    }
                    Log.d("CalendarViewModel", "Preloaded $nextMonth")
                } catch (e: Exception) {
                    Log.e("CalendarViewModel", "Error preloading $nextMonth", e)
                }
            }
        }
    }
    // ==========================================
// ПЕРЕМЕЩЕНИЕ ЭЛЕМЕНТОВ В СВОДКЕ
// ==========================================

    fun reorderEvent(fromIndex: Int, toIndex: Int) {
        val date = _uiState.value.selectedDate
        _uiState.update { state ->
            val updatedPages = state.pages.map { page ->
                val updatedGrid = page.grid.map { column ->
                    column.map { cell ->
                        if (cell?.date == date) {
                            val events = cell.events.toMutableList()
                            if (fromIndex in events.indices && toIndex in events.indices) {
                                val item = events.removeAt(fromIndex)
                                events.add(toIndex, item)
                            }
                            cell.copy(events = events.toImmutableList())
                        } else {
                            cell
                        }
                    }.toImmutableList()
                }.toImmutableList()
                page.copy(grid = updatedGrid)
            }.toImmutableList()
            state.copy(pages = updatedPages)
        }
    }

    fun reorderTask(fromIndex: Int, toIndex: Int) {
        val date = _uiState.value.selectedDate
        _uiState.update { state ->
            val updatedPages = state.pages.map { page ->
                val updatedGrid = page.grid.map { column ->
                    column.map { cell ->
                        if (cell?.date == date) {
                            val tasks = cell.tasks.toMutableList()
                            if (fromIndex in tasks.indices && toIndex in tasks.indices) {
                                val item = tasks.removeAt(fromIndex)
                                tasks.add(toIndex, item)
                            }
                            cell.copy(tasks = tasks.toImmutableList())
                        } else {
                            cell
                        }
                    }.toImmutableList()
                }.toImmutableList()
                page.copy(grid = updatedGrid)
            }.toImmutableList()
            state.copy(pages = updatedPages)
        }
    }

    fun reorderNote(fromIndex: Int, toIndex: Int) {
        val date = _uiState.value.selectedDate
        _uiState.update { state ->
            val updatedPages = state.pages.map { page ->
                val updatedGrid = page.grid.map { column ->
                    column.map { cell ->
                        if (cell?.date == date) {
                            val notes = cell.notes.toMutableList()
                            if (fromIndex in notes.indices && toIndex in notes.indices) {
                                val item = notes.removeAt(fromIndex)
                                notes.add(toIndex, item)
                            }
                            cell.copy(notes = notes.toImmutableList())
                        } else {
                            cell
                        }
                    }.toImmutableList()
                }.toImmutableList()
                page.copy(grid = updatedGrid)
            }.toImmutableList()
            state.copy(pages = updatedPages)
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun loadMonthData(yearMonth: YearMonth): kotlinx.collections.immutable.ImmutableList<CalendarPage> {
        val firstDay = yearMonth.atDay(1)
        val emptyGrid = mapper.map(firstDay)
        val monthData = getCalendarMonthUseCase.invoke(yearMonth).associateBy { it.date }

        val filledGrid = emptyGrid.map { week ->
            week.map { monthData[it?.date] ?: it }.toImmutableList()
        }.toImmutableList()

        return filledGrid
            .chunked(4)
            .map { weekChunk ->
                if (weekChunk.size < 4) {
                    weekChunk + List(4 - weekChunk.size) {
                        List(7) { null as CalendarDay? }.toImmutableList()
                    }
                } else {
                    weekChunk
                }
            }
            .mapIndexed { pageIndex, column ->
                CalendarPage(
                    yearMonth = yearMonth,
                    grid = column.toImmutableList(),
                    isSecondHalf = pageIndex > 0
                )
            }
            .toImmutableList()
    }

    fun onDayClick(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun onShowDetailsClick() {
        _uiState.update { it.copy(showDetailsSheet = true) }
    }

    fun onAddClick(mode: AddMode) {
        _uiState.update { state ->
            if (state.showAddItemSheet) {
                Log.w("CalendarViewModel", "AddItemSheet already open, ignoring click")
                state
            } else {
                state.copy(showAddItemSheet = true, currentAddMode = mode)
            }
        }
    }

    fun closeSheets() {
        _uiState.update {
            it.copy(
                showAddItemSheet = false,
                showDetailsSheet = false,
                currentAddMode = null,
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onAddItem(command: AddItemCommand) {
        if (!isSaving.compareAndSet(false, true)) {
            Log.w("CalendarViewModel", "onAddItem called while saving, ignoring")
            return
        }

        viewModelScope.launch {
            try {
                Log.d("DEBUG_SAVE", "Saving ${command.mode} for ${command.date}")

                when (command.mode) {
                    AddMode.Event -> {
                        Log.d("CalendarViewModel", "Event creating is initiated")

                        // ✅ Логика определения времени
                        val (finalStartTime, finalEndTime, finalIsAllDay, finalIsReminder) = when {
                            // Если "Весь день"
                            command.isAllDay -> {
                                val start = LocalTime.of(0, 0)
                                val end = LocalTime.of(23, 59)
                                Quad(start, end, true, false)
                            }
                            // Если "Напоминание" — одно время
                            command.isReminder -> {
                                val time = command.startTime ?: LocalTime.of(9, 0)
                                Quad(time, time, false, true)
                            }
                            // Обычное событие с временем начала и окончания
                            else -> {
                                val start = command.startTime ?: LocalTime.of(9, 0)
                                val end = command.endTime ?: start.plusHours(1)
                                Quad(start, end, false, false)
                            }
                        }

                        val newEvent = Event(
                            id = null,
                            title = command.title,
                            description = command.description.takeIf { it.isNotBlank() },
                            startDateTime = command.date.atTime(finalStartTime),
                            isAllDay = finalIsAllDay,
                            isReminder = finalIsReminder,
                            isImportant = command.isImportant,
                            recurrenceType = RecurrenceType.NONE,
                            customIntervalDays = 0,
                            recurrenceEndDate = null,
                            createdAt = LocalDateTime.now(),
                            updatedAt = null
                        )

                        // ✅ Сохраняем endTime в description если не весь день и не напоминание
                        // (или можно добавить поле endTime в Event, если нужно)
                        eventRepository.addEvent(newEvent)
                    }

                    AddMode.Task -> {
                        Log.d("CalendarViewModel", "Task creating is initiated")
                        val newTask = Task(
                            id = null,
                            title = command.title,
                            description = command.description.takeIf { it.isNotBlank() },
                            date = command.date,
                            completedAt = null,
                            createdAt = LocalDateTime.now(),
                            updatedAt = null,
                            priority = Priority.MEDIUM,
                            isCompleted = false
                        )
                        taskRepository.addTask(newTask)
                    }

                    AddMode.Note -> {
                        Log.d("CalendarViewModel", "Note creating is initiated")
                        val newNote = Note(
                            id = null,
                            date = command.date,
                            content = command.description,
                            createdAt = LocalDateTime.now(),
                            updatedAt = null,
                            title = command.title
                        )
                        noteRepository.addNote(newNote)
                    }
                }

                val updatedEvents = eventRepository.getEventsForDateRangeOnce(command.date, command.date)
                val updatedTasks = taskRepository.getTasksForDateRangeOnce(command.date, command.date)
                val updatedNotes = noteRepository.getNotesForDateRangeOnce(command.date, command.date)

                updateDayIndicatorsAtomic(command.date, updatedEvents, updatedTasks, updatedNotes)

            } catch (e: Exception) {
                Log.e("VM_ERROR", "onAddItem ERROR", e)
                e.printStackTrace()
            } finally {
                isSaving.set(false)
            }
        }
    }

    private fun updateDayIndicatorsAtomic(
        date: LocalDate,
        newEvents: List<Event>,
        newTasks: List<Task>,
        newNotes: List<Note>
    ) {
        Log.d("updInd", "updateDayIndAtomic called $date")
        Log.i("updInd", "new TasksCount : ${newTasks.size}")
        Log.i("updInd", "new NotesCount : ${newNotes.size}")
        Log.i("updInd", "new EventsCount : ${newEvents.size}")

        _uiState.update { state ->
            var anyPageChanged = false

            val updatedPages = state.pages.map { page ->
                var thisPageChanged = false

                val updatedGrid = page.grid.map { column ->
                    column.map { cell ->
                        if (cell?.date == date) {
                            thisPageChanged = true
                            cell.copy(
                                events = newEvents.toImmutableList(),
                                tasks = newTasks.toImmutableList(),
                                notes = newNotes.toImmutableList()
                            )
                        } else {
                            cell
                        }
                    }.toImmutableList()
                }.toImmutableList()

                if (thisPageChanged) {
                    anyPageChanged = true
                    page.copy(grid = updatedGrid)
                } else {
                    page
                }
            }.toImmutableList()

            // ✅ Также обновляем кэш
            val month = YearMonth.from(date)
            val newCache = if (state.monthsCache.containsKey(month)) {
                state.monthsCache.toPersistentMap().put(month, updatedPages)
            } else {
                state.monthsCache
            }

            if (anyPageChanged) {
                state.copy(pages = updatedPages, monthsCache = newCache)
            } else {
                state
            }
        }
    }
}