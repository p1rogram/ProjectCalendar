package com.example.projectcalendar.presentation.navigation

object ScreenRoutes {
    const val CALENDAR = "calendar"
    const val EVENT_DETAIL = "event_detail/{eventId}"
    const val TASK_DETAIL = "task_detail/{taskId}"

    // Helper функции для навигации с аргументами
    fun eventDetail(eventId: Long) = "event_detail/$eventId"
    fun taskDetail(taskId: Long) = "task_detail/$taskId"
}