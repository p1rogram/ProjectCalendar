package com.example.projectcalendar.presentation.ui.calendar.components

import com.example.projectcalendar.presentation.ui.common.AddMode
import java.time.LocalDate
import java.time.LocalTime

// Команда для передачи данных в ViewModel
data class AddItemCommand(
    val mode: AddMode,
    val date: LocalDate,
    val title: String,
    val description: String,
    val time: LocalTime? = null, // Нужно для Event и Reminder :TODO(REFRESH)
    val isImportant: Boolean
)
