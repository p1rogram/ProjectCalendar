package com.example.projectcalendar.presentation

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.navigation.compose.rememberNavController
import com.example.projectcalendar.presentation.navigation.AppNavGraph
import com.example.projectcalendar.presentation.ui.theme.GrayBack
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                scrim = GrayBack.toArgb() // Цвет "шторки" (полупрозрачный оверлей)
            ),
            navigationBarStyle = SystemBarStyle.dark(
                scrim = GrayBack.toArgb()
            )
        )

        setContent() {

            val navController = rememberNavController()
            Scaffold(modifier = Modifier.fillMaxSize().background(GrayBack)) { innerPadding ->
                AppNavGraph(navController = navController)
            }
        }
    }
}