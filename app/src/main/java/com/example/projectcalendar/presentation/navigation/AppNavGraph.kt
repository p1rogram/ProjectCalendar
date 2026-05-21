package com.example.projectcalendar.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.projectcalendar.presentation.ui.screen.CalendarScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.CALENDAR) {

        composable(Routes.CALENDAR) {
            CalendarScreen(
                // Передаём навигационные события наверх
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onShowSnackbar = { /* TODO: подключить SnackbarHost */ }
            )
        }

        composable(Routes.SETTINGS) {
            // SettingsScreen(onNavigateBack = { navController.popBackStack() })
            // Пока заглушка, чтобы граф компилировался
        }
    }
}