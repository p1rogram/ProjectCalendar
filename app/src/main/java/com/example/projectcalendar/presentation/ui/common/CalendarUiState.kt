package com.example.projectcalendar.presentation.ui.common

import com.example.projectcalendar.domain.model.CalendarDay
import java.time.LocalDate
import java.time.YearMonth
data class CalendarUiState(
    val status: LoadStatus = LoadStatus.Loading,
    val pages : List<CalendarPage> = emptyList(),
    val initialPageIndex : Int = 0,
    val selectedDate : LocalDate = LocalDate.now(),
    val showAddItemSheet : Boolean = false,
    val showDetailsSheet : Boolean = false,
    val currentAddMode: AddMode? = null
) {
}
sealed class LoadStatus(){
    object Loading : LoadStatus()
    object Success : LoadStatus()
    object Error : LoadStatus()
}
sealed class AddMode(){
    object Event : AddMode()
    object Reminder : AddMode()
    object Note : AddMode()
}

data class CalendarPage(
    val yearMonth: YearMonth,
    val grid : List<List<CalendarDay?>>,
    val isSecondHalf: Boolean
){}
