package com.example.projectcalendar.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.projectcalendar.presentation.ui.calendar.CalendarScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ScreenRoutes.CALENDAR
    ) {
        composable(route = ScreenRoutes.CALENDAR) {
            CalendarScreen()
        }

        // Здесь будут другие экраны в будущем:
        // composable(route = ScreenRoutes.EVENT_DETAIL) { ... }
        // composable(route = ScreenRoutes.TASK_DETAIL) { ... }
    }
}