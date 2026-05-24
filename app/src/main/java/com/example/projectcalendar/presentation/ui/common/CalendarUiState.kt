package com.example.projectcalendar.presentation.ui.common

import androidx.compose.runtime.Immutable
import com.example.projectcalendar.domain.model.CalendarDay
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDate
import java.time.YearMonth

@Immutable
data class CalendarUiState(
    val status: LoadStatus = LoadStatus.Loading,
    val pages: ImmutableList<CalendarPage> = persistentListOf(),  // ✅
    val initialPageIndex: Int = 0,
    val selectedDate: LocalDate = LocalDate.now(),
    val showAddItemSheet: Boolean = false,
    val showDetailsSheet: Boolean = false,
    val currentAddMode: AddMode? = null
)

sealed class LoadStatus {
    object Loading : LoadStatus()
    object Success : LoadStatus()
    object Error : LoadStatus()
}

sealed class AddMode {
    object Event : AddMode()
    object Task : AddMode()
    object Note : AddMode()
}

@Immutable
data class CalendarPage(
    val yearMonth: YearMonth,
    val grid: ImmutableList<ImmutableList<CalendarDay?>>,  // ✅
    val isSecondHalf: Boolean
)