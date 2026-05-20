package com.example.projectcalendar.presentation.ui.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.projectcalendar.domain.model.CalendarDay
import com.example.projectcalendar.data.utils.getMonthDays
import com.example.projectcalendar.data.utils.weekOffset
import java.time.LocalDate

class CalendarGridMapper {

    @RequiresApi(Build.VERSION_CODES.O)
    fun map(date: LocalDate): List<List<CalendarDay?>> {
        val offset = weekOffset(date)      // 0..6 пустых клеток в начале
        val daysCount = getMonthDays(date) // 28..31 реальных дней

        val gridItems = mutableListOf<CalendarDay?>()

        // 1. Отступ начала месяца (пустые клетки)
        repeat(offset) {
            gridItems.add(null)
        }

        // 2. Реальные дни месяца (пока с пустыми списками данных)
        for (dayNumber in 1..daysCount) {
            val currentDate = date.withDayOfMonth(dayNumber)
            gridItems.add(
                CalendarDay(
                    date = currentDate,
                    events = emptyList(),
                    tasks = emptyList(),
                    notes = emptyList()
                )
            )
        }

        // 3. Отступ конца месяца (добиваем до кратности 7)
        while (gridItems.size % 7 != 0) {
            gridItems.add(null)
        }

        // 4. Разбиваем плоский список на колонки по 7 ячеек
        return gridItems.chunked(7)
    }
}