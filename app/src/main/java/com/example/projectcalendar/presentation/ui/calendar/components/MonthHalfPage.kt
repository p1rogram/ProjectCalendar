package com.example.projectcalendar.presentation.ui.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectcalendar.domain.model.CalendarDay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MonthHalfPage(
    days: List<CalendarDay>,
    selectedDate: LocalDate?,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    // Дни недели заголовка (Пн, Вт, Ср...)
    val daysOfWeek = listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY
    )

    Column(modifier = modifier.fillMaxSize()) {
        // Заголовок с днями недели
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            daysOfWeek.forEach { dayOfWeek ->
                Text(
                    text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("ru")),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Сетка дней - вертикальная матрица (сначала сверху вниз, потом слева направо)
        // Используем LazyHorizontalGrid с 7 строками (дни недели), элементы заполняются по строкам
        // Это даст эффект: сначала заполняем Пн,Вт,Ср... вниз, затем переходим к следующей колонке
        LazyHorizontalGrid(
            rows = GridCells.Fixed(7),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(days) { day ->
                DayCell(
                    calendarDay = day,
                    isSelected = day.date == selectedDate,
                    onClick = { onDayClick(day.date) }
                )
            }
        }
    }
}

@Composable
fun DayCell(
    calendarDay: CalendarDay,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isToday = calendarDay.isToday
    val isPast = calendarDay.isPast && !isToday
    
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primary
                isToday -> MaterialTheme.colorScheme.secondaryContainer
                else -> Color.Transparent
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else if (isToday) MaterialTheme.colorScheme.secondaryContainer
                    else Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(4.dp)
            ) {
                Text(
                    text = calendarDay.date.dayOfMonth.toString(),
                    fontSize = if (isToday) 16.sp else 14.sp,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimary
                        isToday -> MaterialTheme.colorScheme.onSecondaryContainer
                        isPast -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                
                // Индикаторы событий и задач
                Spacer(modifier = Modifier.height(2.dp))
                
                if (calendarDay.hasEvents || calendarDay.hasTasks) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        if (calendarDay.hasEvents) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(
                                        if (calendarDay.hasImportantEvent) 
                                            MaterialTheme.colorScheme.error 
                                        else 
                                            MaterialTheme.colorScheme.primary
                                    )
                            )
                        }
                        if (calendarDay.hasTasks) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(
                                        if (calendarDay.taskProgress >= 1.0f) 
                                            MaterialTheme.colorScheme.tertiary 
                                        else 
                                            MaterialTheme.colorScheme.secondary
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}