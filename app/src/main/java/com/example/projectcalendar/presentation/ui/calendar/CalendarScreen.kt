package com.example.projectcalendar.presentation.ui.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectcalendar.presentation.ui.calendar.components.MonthHalfPage
import com.example.projectcalendar.presentation.viewmodel.CalendarViewModel
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // Pager для двух половинок месяца + навигация между месяцами
    // Используем pager с 3 страницами: [пред. месяц вторая половина] [текущий месяц первая/вторая] [след. месяц первая половина]
    // Но для простоты реализуем через состояние currentPage в UI State
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Заголовок с названием месяца
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "<",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(8.dp)
            )
            
            Text(
                text = uiState.currentMonth.month.getDisplayName(TextStyle.FULL, Locale("ru"))
                        .replaceFirstChar { it.uppercase() } + " " + uiState.currentMonth.year,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = ">",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(8.dp)
            )
        }
        
        // Индикатор загрузки
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Отображение текущей половины месяца
            val currentDays = if (uiState.currentPage == 0) {
                uiState.firstHalfDays
            } else {
                uiState.secondHalfDays
            }
            
            // Индикатор страницы (1/2 или 2/2)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (uiState.currentPage == 0) "Часть 1" else "Часть 2",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            MonthHalfPage(
                days = currentDays,
                selectedDate = uiState.selectedDate,
                onDayClick = { date ->
                    viewModel.selectDate(date)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            )
            
            // Навигационные кнопки
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                androidx.compose.material3.Button(
                    onClick = { 
                        if (uiState.currentPage == 0 && uiState.firstHalfDays.isNotEmpty()) {
                            // Переход к предыдущему месяцу
                            val prevMonth = uiState.currentMonth.minusMonths(1)
                            viewModel.loadMonth(prevMonth)
                        } else if (uiState.currentPage == 1) {
                            // Переход к первой половине текущего месяца
                            // viewModel.navigateToPreviousPage()
                        }
                    },
                    enabled = uiState.currentPage == 0
                ) {
                    Text("Пред. месяц")
                }
                
                androidx.compose.material3.Button(
                    onClick = {
                        if (uiState.currentPage == 0 && uiState.secondHalfDays.isNotEmpty()) {
                            // Переход ко второй половине текущего месяца
                            // viewModel.navigateToNextPage()
                        } else if (uiState.currentPage == 1) {
                            // Переход к следующему месяцу
                            val nextMonth = uiState.currentMonth.plusMonths(1)
                            viewModel.loadMonth(nextMonth)
                        }
                    },
                    enabled = true
                ) {
                    Text("След. месяц")
                }
            }
        }
        
        // Ошибка
        uiState.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}