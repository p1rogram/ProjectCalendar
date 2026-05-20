package com.example.projectcalendar.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectcalendar.domain.model.CalendarDay
import com.example.projectcalendar.domain.usecase.calendar.GetCalendarUseCase
import com.example.projectcalendar.presentation.ui.common.CalendarUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class CalendarViewModel(
    private val getCalendarUseCase: GetCalendarUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadMonth(YearMonth.now())
    }

    fun loadMonth(yearMonth: YearMonth) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                currentMonth = yearMonth,
                currentPage = 0,
                isLoading = true
            )
            
            try {
                val allDays = CalendarUiState.generateMonthDays(yearMonth)
                
                // Загружаем данные для каждого дня (в реальном приложении можно оптимизировать)
                val loadedDays = allDays.map { day ->
                    try {
                        getCalendarUseCase(day.date)
                    } catch (e: Exception) {
                        day
                    }
                }
                
                val (firstHalf, secondHalf) = CalendarUiState.splitMonthIntoHalves(loadedDays)
                
                _uiState.value = _uiState.value.copy(
                    firstHalfDays = firstHalf,
                    secondHalfDays = secondHalf,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun navigateToNextPage() {
        val currentState = _uiState.value
        if (currentPage == 1) {
            // Переход к следующему месяцу
            val nextMonth = currentState.currentMonth.plusMonths(1)
            loadMonth(nextMonth)
        } else {
            _uiState.value = currentState.copy(currentPage = 1)
        }
    }

    fun navigateToPreviousPage() {
        val currentState = _uiState.value
        if (currentPage == 0) {
            // Переход к предыдущему месяцу
            val prevMonth = currentState.currentMonth.minusMonths(1)
            loadMonth(prevMonth)
        } else {
            _uiState.value = currentState.copy(currentPage = 0)
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
    }

    private val currentPage: Int
        get() = _uiState.value.currentPage
}