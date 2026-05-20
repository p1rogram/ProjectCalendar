package com.example.projectcalendar.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectcalendar.domain.usecase.calendar.GetCalendarMonthUseCase
import com.example.projectcalendar.presentation.ui.common.AddMode
import com.example.projectcalendar.presentation.ui.common.CalendarPage
import com.example.projectcalendar.presentation.ui.common.CalendarUiState
import com.example.projectcalendar.presentation.ui.common.LoadStatus
import com.example.projectcalendar.presentation.ui.mapper.CalendarGridMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * ViewModel экрана календаря.
 * Управляется Hilt: зависимости приходят через конструктор,
 * жизненный цикл привязан к экрану, корутины отменяются автоматически.
 */
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val mapper: CalendarGridMapper,
    private val getCalendarMonthUseCase: GetCalendarMonthUseCase
) : ViewModel() {

    // Приватный изменяемый поток состояния
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
    fun loadMonth(year: Int, month: Int) {
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
            }
        }
    }

    // === Обработчики событий UI ===

    fun onDayClick(date: LocalDate) {
        _uiState.value = _uiState.value.copy(
            selectedDate = date,
        )
    }
    fun onShowDetailsClick(){
        _uiState.value = _uiState.value.copy(
            showDetailsSheet = true
        )
    }
    fun onAddClick(mode : AddMode){
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
}