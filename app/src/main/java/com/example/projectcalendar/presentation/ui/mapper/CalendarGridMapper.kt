package com.example.projectcalendar.presentation.ui.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.projectcalendar.domain.model.CalendarDay
import com.example.projectcalendar.data.utils.getMonthDays
import com.example.projectcalendar.data.utils.weekOffset
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.time.LocalDate
import javax.inject.Inject

class CalendarGridMapper @Inject constructor() {

    @RequiresApi(Build.VERSION_CODES.O)
    fun map(date: LocalDate): ImmutableList<ImmutableList<CalendarDay?>> {
        val offset = weekOffset(date)
        val daysCount = getMonthDays(date)
        val gridItems = mutableListOf<CalendarDay?>()

        // 1. Отступ начала месяца (пустые клетки)
        repeat(offset) {
            gridItems.add(null)
        }

        // 2. Реальные дни месяца
        for (dayNumber in 1..daysCount) {
            val currentDate = date.withDayOfMonth(dayNumber)
            gridItems.add(
                CalendarDay(
                    date = currentDate,
                    // Пустые immutable списки для заглушек
                )
            )
        }

        // 3. Отступ конца месяца (добиваем до кратности 7)
        while (gridItems.size % 7 != 0) {
            gridItems.add(null)
        }

        // 4. Разбиваем и конвертируем в ImmutableList
        return gridItems
            .chunked(7)
            .map { it.toImmutableList() }   // ✅ внутренние списки
            .toImmutableList()              // ✅ внешний список
    }
}