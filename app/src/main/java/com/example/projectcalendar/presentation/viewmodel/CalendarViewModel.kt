    package com.example.projectcalendar.presentation.viewmodel

    import android.os.Build
    import android.util.Log
    import androidx.annotation.RequiresApi
    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.viewModelScope
    import com.example.projectcalendar.data.repository.EventRepository
    import com.example.projectcalendar.data.repository.NoteRepository
    import com.example.projectcalendar.data.repository.TaskRepository
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
    import com.example.projectcalendar.presentation.ui.components.AddItemCommand
    import com.example.projectcalendar.presentation.ui.mapper.CalendarGridMapper
    import dagger.hilt.android.lifecycle.HiltViewModel
    import kotlinx.coroutines.flow.MutableStateFlow
    import kotlinx.coroutines.flow.StateFlow
    import kotlinx.coroutines.flow.asStateFlow
    import kotlinx.coroutines.flow.first
    import kotlinx.coroutines.launch
    import java.time.LocalDate
    import java.time.LocalDateTime
    import java.time.YearMonth
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
                pages = emptyList(),
                initialPageIndex = 0,
                selectedDate = LocalDate.now(),
                showAddItemSheet = false,
                showDetailsSheet = false,
                currentAddMode = null
            )
        )
        val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

        @RequiresApi(Build.VERSION_CODES.O)
        fun loadMonth(year: Int, month: Int, showLoading: Boolean = true) {
            viewModelScope.launch {
                try {
                    if (_uiState.value.selectedDate.year != year || _uiState.value.selectedDate.monthValue != month) {
                        _uiState.value = _uiState.value.copy(selectedDate = LocalDate.of(year, month, 1))
                    }
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Loading)

                    val firstDay = LocalDate.of(year, month, 1)
                    val emptyGrid = mapper.map(firstDay)
                    val monthData = getCalendarMonthUseCase.invoke(YearMonth.of(year, month)).associateBy { it.date }
                    val filledGrid = emptyGrid.map { week -> week.map { monthData[it?.date] ?: it } }

                    _uiState.value = _uiState.value.copy(
                        pages = filledGrid
                            .chunked(4)
                            .map { weekChunk ->
                                if (weekChunk.size < 4) {
                                    weekChunk + List(4 - weekChunk.size) { List(7) { null } }
                                } else {
                                    weekChunk
                                }
                            }
                            .mapIndexed { pageIndex, column ->
                                CalendarPage(
                                    yearMonth = YearMonth.of(year, month),
                                    grid = column,
                                    isSecondHalf = pageIndex > 0
                                )
                            },
                        status = LoadStatus.Success
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(status = LoadStatus.Error)
                    Log.e("CalendarViewModel", "Error loading month", e)
                }
            }
        }

        // === Обработчики событий UI ===

        fun onDayClick(date: LocalDate) {
            _uiState.value = _uiState.value.copy(selectedDate = date)
        }

        fun onShowDetailsClick() {
            _uiState.value = _uiState.value.copy(showDetailsSheet = true)
        }

        fun onAddClick(mode: AddMode) {
            _uiState.value = _uiState.value.copy(
                showAddItemSheet = true,
                currentAddMode = mode
            )
        }

        fun closeSheets() {
            _uiState.value = _uiState.value.copy(
                showAddItemSheet = false,
                showDetailsSheet = false,
                currentAddMode = null,
            )
        }

        // === Сохранение новых элементов ===

        // В начале класса CalendarViewModel добавь:
        private var isSaving = false

        @RequiresApi(Build.VERSION_CODES.O)
        fun onAddItem(command: AddItemCommand) {
            if (isSaving) return
            isSaving = true

            viewModelScope.launch {
                try {
                    Log.d("DEBUG_SAVE", "Saving ${command.mode} for ${command.date}")
                    when (command.mode) {
                        AddMode.Event -> {
                            val newEvent = Event(
                                id = null,
                                title = command.title,
                                description = command.description.takeIf { it.isNotBlank() },
                                startDateTime = command.date.atTime(command.time ?: java.time.LocalTime.MIDNIGHT),
                                isAllDay = command.time == null,
                                isReminder = false,
                                isImportant = true,
                                recurrenceType = RecurrenceType.NONE,
                                customIntervalDays = 0,
                                recurrenceEndDate = null,
                                createdAt = LocalDateTime.now(),
                                updatedAt = null
                            )
                            eventRepository.addEvent(newEvent)

                            // ✅ Обновление индикаторов БЕЗ вложенного launch
                            val updatedEvents = eventRepository.getEventsForDateRange(command.date, command.date).first()
                            val updatedTasks = taskRepository.getTasksForDateRange(command.date, command.date).first()
                            val updatedNotes = noteRepository.getNotesForDateRange(command.date, command.date).first()
                            updateDayIndicators(command.date, updatedEvents, updatedTasks, updatedNotes)
                        }

                        AddMode.Task -> {
                            Log.i("CalendarViewModel", "Task creating is initiated")
                            val newTask = Task(
                                id = null,
                                title = command.title,  // ✅ Исправлено: было ":TOOO"
                                description = command.description.takeIf { it.isNotBlank() },  // ✅ Исправлено: было ":TODO"
                                date = command.date,
                                completedAt = null,
                                createdAt = LocalDateTime.now(),
                                updatedAt = null,
                                priority = Priority.MEDIUM,
                                isCompleted = false
                            )
                            taskRepository.addTask(newTask)

                            // ✅ Обновление индикаторов БЕЗ вложенного launch
                            val updatedEvents = eventRepository.getEventsForDateRange(command.date, command.date).first()
                            val updatedTasks = taskRepository.getTasksForDateRange(command.date, command.date).first()
                            val updatedNotes = noteRepository.getNotesForDateRange(command.date, command.date).first()
                            updateDayIndicators(command.date, updatedEvents, updatedTasks, updatedNotes)
                        }

                        AddMode.Note -> {
                            val newNote = Note(
                                id = null,
                                date = command.date,
                                content = command.description,
                                createdAt = LocalDateTime.now(),
                                updatedAt = null
                            )
                            noteRepository.addNote(newNote)

                            // ✅ Обновление индикаторов БЕЗ вложенного launch
                            val updatedEvents = eventRepository.getEventsForDateRange(command.date, command.date).first()
                            val updatedTasks = taskRepository.getTasksForDateRange(command.date, command.date).first()
                            val updatedNotes = noteRepository.getNotesForDateRange(command.date, command.date).first()
                            updateDayIndicators(command.date, updatedEvents, updatedTasks, updatedNotes)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("VM_ERROR", "onAddItem ERROR", e)
                    e.printStackTrace()
                } finally {
                    isSaving = false  // ✅ Сброс флага
                }
            }
        }

        // Вспомогательный метод для перезагрузки текущего месяца после сохранения
        @RequiresApi(Build.VERSION_CODES.O)
        private fun refreshCurrentMonth() {
            val currentMonth = _uiState.value.pages.firstOrNull()?.yearMonth
            currentMonth?.let { ym ->
                loadMonth(ym.year, ym.monthValue, showLoading = false) // ← 🔧 Ключевое изменение
            }
        }
        fun updateDayIndicators(date: LocalDate, newEvents: List<Event> = emptyList(),
                                newTasks: List<Task> = emptyList(), newNotes: List<Note> = emptyList()) {

            val currentPages = _uiState.value.pages
            val updatedPages = currentPages.map { page ->
                val updatedGrid = page.grid.map { column ->
                    column.map { cell ->
                        // Обновляем ТОЛЬКО если дата совпадает
                        if (cell?.date == date) {
                            cell.copy(
                                events = newEvents,
                                tasks = newTasks,
                                notes = newNotes
                            )
                        } else {
                            cell // Возвращаем ту же ссылку → Compose не перерисует
                        }
                    }
                }
                // Создаём новую страницу только если сетка реально изменилась
                if (updatedGrid != page.grid) page.copy(grid = updatedGrid) else page
            }

            // Обновляем состояние только если что-то изменилось
            if (updatedPages != currentPages) {
                _uiState.value = _uiState.value.copy(pages = updatedPages)
            }
        }
    }
