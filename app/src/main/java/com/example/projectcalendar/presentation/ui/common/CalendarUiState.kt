package com.example.projectcalendar.presentation.ui.common

import com.example.projectcalendar.domain.model.CalendarDay
import java.time.LocalDate
import java.time.YearMonth
/////////РАЗОБРАТЬ
data class CalendarUiState(
    val status: LoadStatus,
    val pages : List<CalendarPage>,
    val initialPageIndex : Int,
    val selectedDate: LocalDate?,
    val showAddItemSheet: Boolean
) {
}
sealed class LoadStatus(){
    object Loading : LoadStatus()
    object Success : LoadStatus()
    object Error : LoadStatus()
}
data class CalendarPage(
    val yearMonth: YearMonth,
    val days : List<CalendarDay>,
    val isSecondHalf: Boolean
){

}
