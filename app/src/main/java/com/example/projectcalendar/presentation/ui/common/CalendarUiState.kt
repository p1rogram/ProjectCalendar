package com.example.projectcalendar.presentation.ui.common

import com.example.projectcalendar.domain.model.CalendarDay
import java.time.LocalDate
import java.time.YearMonth

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val currentPage: Int = 0, // 0 = первая половина, 1 = вторая половина
    val selectedDate: LocalDate = LocalDate.now(),
    val firstHalfDays: List<CalendarDay> = emptyList(),
    val secondHalfDays: List<CalendarDay> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    companion object {
        fun generateMonthDays(yearMonth: YearMonth): List<CalendarDay> {
            val daysInMonth = yearMonth.lengthOfMonth()
            return (1..daysInMonth).map { day ->
                val date = yearMonth.atDay(day)
                CalendarDay(
                    date = date,
                    events = emptyList(),
                    tasks = emptyList(),
                    notes = emptyList()
                )
            }
        }
        
        fun splitMonthIntoHalves(days: List<CalendarDay>): Pair<List<CalendarDay>, List<CalendarDay>> {
            val midpoint = (days.size + 1) / 2
            val firstHalf = days.take(midpoint)
            val secondHalf = days.drop(midpoint)
            return firstHalf to secondHalf
        }
    }
}