package com.example.projectcalendar.presentation.ui.screen

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Brush.Companion.radialGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SweepGradientShader
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.projectcalendar.domain.model.CalendarDay
import com.example.projectcalendar.presentation.ui.common.AddMode
import com.example.projectcalendar.presentation.ui.common.CalendarPage
import com.example.projectcalendar.presentation.ui.common.LoadStatus
import com.example.projectcalendar.presentation.ui.calendar.components.AddItemCommand
import com.example.projectcalendar.presentation.ui.theme.*
import com.example.projectcalendar.presentation.viewmodel.CalendarViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale as JavaLocale

private val ScreenBackground = GrayBack
private val week: List<String> = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
private val brush = radialGradient(
    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.2f),)
)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel(),
    @Suppress("UNUSED_PARAMETER") onNavigateToSettings: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onShowSnackbar: (String) -> Unit = {}
) {
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window
        window?.setBackgroundDrawable(ColorDrawable(ScreenBackground.toArgb()))
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(
        initialPage = state.initialPageIndex,
        pageCount = { state.pages.size }
    )

    LaunchedEffect(Unit) {
        val today = LocalDate.now()
        viewModel.loadMonth(today.year, today.monthValue)
    }

    Scaffold(
        topBar = {
            CalendarTopBar(
                yearMonth = state.pages.getOrNull(pagerState.currentPage)?.yearMonth,
                onPrevClick = {
                    val targetMonth = state.pages.getOrNull(0)?.yearMonth?.minusMonths(1)
                    targetMonth?.let { viewModel.loadMonth(it.year, it.monthValue) }
                },
                onNextClick = {
                    val targetMonth = state.pages.getOrNull(0)?.yearMonth?.plusMonths(1)
                    targetMonth?.let { viewModel.loadMonth(it.year, it.monthValue) }
                }
            )
        },
        bottomBar = {
            BottomActionButtons(
                onAddClick = { mode -> viewModel.onAddClick(mode) },
                onDetailsClick = { viewModel.onShowDetailsClick() }
            )
        },
        containerColor = ScreenBackground,
        contentColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ScreenBackground)
        ) {
            when (state.status) {
                LoadStatus.Loading -> LoadingIndicator()
                LoadStatus.Error -> ErrorView(onRetry = {
                    state.pages.getOrNull(pagerState.currentPage)?.yearMonth
                        ?.let { viewModel.loadMonth(it.year, it.monthValue) }
                })

                LoadStatus.Success -> {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(ScreenBackground),
                        beyondViewportPageCount = 1  // ✅ Исправлено: правильное имя параметра
                    ) { page ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            state.pages.getOrNull(page)?.let { pageData ->
                                CalendarMonthGrid(
                                    grid = pageData.grid,
                                    selectedDate = state.selectedDate,
                                    onDayClick = viewModel::onDayClick
                                )
                            }
                        }
                    }
                }
            }

            if (state.showAddItemSheet && state.currentAddMode != null) {
                val fixedMode = state.currentAddMode!!
                val fixedDate = state.selectedDate

                AddItemSheet(
                    mode = fixedMode,
                    date = fixedDate,
                    onDismiss = { viewModel.closeSheets() },
                    onSave = { command ->
                        viewModel.onAddItem(command)
                        viewModel.closeSheets()
                    }
                )
            }

            if (state.showDetailsSheet) {
                DayDetailsSheet(
                    date = state.selectedDate,
                    dayData = findDayData(state.pages, state.selectedDate),
                    onDismiss = viewModel::closeSheets
                )
            }
        }
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//private fun CalendarTopBar(
//    yearMonth: YearMonth?,
//    onPrevClick: () -> Unit,
//    onNextClick: () -> Unit,
//) {
//    TopAppBar(
//        colors = TopAppBarDefaults.topAppBarColors(
//            containerColor = ScreenBackground,
//            titleContentColor = Color.White,
//            navigationIconContentColor = Color.White
//        ),
//        title = {
//            val monthName = yearMonth?.month?.getDisplayName(
//                TextStyle.FULL,
//                JavaLocale.forLanguageTag("ru-RU")
//            ) ?: ""
//            val capitalized = monthName.replaceFirstChar {
//                if (it.isLowerCase()) it.uppercaseChar() else it
//            }
//            Text(
//                text = "$capitalized ${yearMonth?.year ?: ""}" + "г.",
//                fontWeight = FontWeight.Bold,
//                maxLines = 1
//            )
//        },
//        navigationIcon = { IconButton(onClick = onPrevClick) { Text("←") } },
//        actions = { IconButton(onClick = onNextClick) { Text("→") } }
//    )
//}

private val PanelShape = RoundedCornerShape(20.dp)
private val PanelBorder = Color(0xFF9A9A9A) // Серая обводка как на макете
private val PanelBg = Color(0xFF1A1A1A)     // Тёмный фон контейнеров
////////////////////////////////////
@Composable
private fun CalendarTopBar(
    yearMonth: YearMonth?,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(top = 10.dp)
            .border(2.dp, PanelBorder, PanelShape)
            .clip(PanelShape)
            .background(PanelBg)
            .background(brush = brush),
        verticalAlignment = Alignment.CenterVertically
            ,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // ← Кнопка
        Box(
            modifier = Modifier
                .weight(1f)
                .clickable(                interactionSource = remember { MutableInteractionSource() },
                    indication = null, // Убираем ripple
                    onClick = onPrevClick)
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("←", color = Color.White, fontSize = 20.sp)
        }

        // Месяц и год по центру
        Column(
            modifier = Modifier
                .weight(2f)
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val monthName = yearMonth?.month?.getDisplayName(
                TextStyle.FULL,
                JavaLocale.forLanguageTag("ru-RU")
            ) ?: ""
            val capitalized = monthName.replaceFirstChar {
                if (it.isLowerCase()) it.uppercaseChar() else it
            }
            Text(
                text = capitalized,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${yearMonth?.year ?: ""}г.",
                color = Color.White,
                fontSize = 16.sp
            )
        }

        // → Кнопка
        Box(
            modifier = Modifier
                .weight(1f)
                .clickable(                interactionSource = remember { MutableInteractionSource() },
                    indication = null, // Убираем ripple
                    onClick = onNextClick)
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("→", color = Color.White, fontSize = 20.sp)
        }
    }
}
////////////////////////////////////////////////////
//@Composable
//private fun BottomActionButtons(
//    onAddClick: (AddMode) -> Unit,
//    onDetailsClick: () -> Unit
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp),
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//        Box {
//            var showMenu by remember { mutableStateOf(false) }
//            FilledIconButton(onClick = { showMenu = true }) {
//                Icon(Icons.Default.Add, contentDescription = "Добавить")
//            }
//            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
//                DropdownMenuItem(
//                    text = { Text("Событие") },
//                    onClick = { onAddClick(AddMode.Event); showMenu = false }
//                )
//                DropdownMenuItem(
//                    text = { Text("Задача") },
//                    onClick = { onAddClick(AddMode.Task); showMenu = false }
//                )
//                DropdownMenuItem(
//                    text = { Text("Заметка") },
//                    onClick = { onAddClick(AddMode.Note); showMenu = false }
//                )
//            }
//        }
//        FilledTonalIconButton(onClick = onDetailsClick) {
//            Icon(Icons.Default.Info, contentDescription = "Сводка")
//        }
//    }
//}
/////////////////////////////////////
@Composable
private fun BottomActionButtons(
    onAddClick: (AddMode) -> Unit,
    onDetailsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .padding(bottom = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 🔘 Левая кнопка: меню добавления
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .border(2.dp, PanelBorder, PanelShape)
                .clip(PanelShape)
                .background(PanelBg)
                .background(brush = brush)
                .clickable {
                    // Открываем меню добавления
                    // (логика меню вынесена в DropdownMenu внутри Box ниже)
                }
        ) {
            // Вынесенное меню для кнопки "+"
            var showMenu by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showMenu = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Добавить",
                    tint = Color.White
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                containerColor = PanelBg  // Тёмный фон меню
            ) {
                DropdownMenuItem(
                    text = { Text("Событие", color = Color.White) },  // ✅ Цвет текста напрямую
                    onClick = { onAddClick(AddMode.Event); showMenu = false }
                )
                DropdownMenuItem(
                    text = { Text("Задача", color = Color.White) },  // ✅ Цвет текста напрямую
                    onClick = { onAddClick(AddMode.Task); showMenu = false }
                )
                DropdownMenuItem(
                    text = { Text("Заметка", color = Color.White) },  // ✅ Цвет текста напрямую
                    onClick = { onAddClick(AddMode.Note); showMenu = false }
                )
            }
        }

        // 🔘 Центральная кнопка: сводка (шире)
        Box(
            modifier = Modifier
                .weight(2f)
                .height(48.dp)
                .border(2.dp, PanelBorder, PanelShape)
                .clip(PanelShape)
                .background(PanelBg)
                .clickable(onClick = onDetailsClick)
                .background(brush = brush),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = "Сводка",
                tint = Color.White
            )
        }

        // 🔘 Правая кнопка: (зарезервировано под будущую фичу)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .border(2.dp, PanelBorder, PanelShape)
                .clip(PanelShape)
                .background(PanelBg)
                .clickable { /* TODO: добавить действие */ }
                .background(brush = brush),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = "Настройки",
                tint = Color.White
            )
            // Пока пусто или можно добавить иконку
        }
    }
}
/////////////////////////////////////////////////////////////
@Composable
private fun CalendarMonthGrid(
    grid: List<List<CalendarDay?>>,
    selectedDate: LocalDate,
    onDayClick: (LocalDate) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            week.forEach { dayName ->
                InfoCell(container = dayName)
            }
        }

        grid.forEach { weekColumn ->
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                weekColumn.forEach { day ->
                    key(day?.date, day?.hasImportantEvent, day?.hasUnimportantEvent,
                        day?.hasTasksOrNotes) {
                        DayCell(
                            day = day,
                            isSelected = day?.date == selectedDate,
                            onClick = { day?.date?.let(onDayClick) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCell(container: String) {
    val shape = RoundedCornerShape(16.dp)
    val backgroundColor = WeekFill
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(backgroundColor)
            .background(brush)
            .border(3.dp, WeekBorder, shape)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = container,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 8.em,
            maxLines = 1
        )
    }
}

@Composable
private fun DayCell(
    day: CalendarDay?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        day?.hasImportantEvent == true -> EventFill
        day?.isToday == true -> Color(0xFF4b6475)
        else -> CellFill
    }

    val borderColor = if (isSelected) SelectBorder else CellFrsBorder
    val secBorderColor = when {
        day?.hasImportantEvent == true -> EventBorder
        else -> CellSecBorder
    }
    val borderWidth = 3.dp
    val shape = RoundedCornerShape(80.dp)

    val gradientBorder = BorderStroke(
        width = 4.dp,
        brush = radialGradient(
            colors = listOf(
                Color.Black.copy(alpha = 0.6f),
                Color.Transparent
            ),
            center = Offset(0.5f, 0.5f),
            radius = 0.6f
        )
    )

    // ✅ Вычисляем цвет индикатора по новой логике
    val indicatorColor = when {
        day == null -> null
        day.hasUnimportantEvent -> Color.Red  // Невыжное событие = красный
        day.hasTasksOrNotes -> Color.Yellow   // Задачи или заметки = жёлтый
        else -> null  // Только важные события или пусто = нет индикатора
    }

    Box {
        // Основная ячейка
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .clip(shape)
                .background(if (day != null) backgroundColor else CellFillEmpty)
                .background(brush = brush)
                .then(if (isSelected) Modifier.border(gradientBorder, shape) else Modifier)
                .border(borderWidth, if (day != null) borderColor else CellFrsBorderEmpty, shape)
                .padding(3.dp)
                .border(borderWidth, if (day != null) secBorderColor else CellSecBorderEmpty, shape)
                .clickable(enabled = day != null, onClick = onClick)
                .padding(4.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (day != null) {
                Text(
                    text = day.date.dayOfMonth.toString(),
                    fontWeight = if (day.isToday) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (day.isPast) Color.Gray else Color.White,
                    fontSize = 11.5.em,
                    maxLines = 1
                )
            }
        }

        // ✅ Индикатор в левом верхнем углу, поверх всех обводок
        if (indicatorColor != null) {
            Box(
                modifier = Modifier
                    .size(25.dp)
                    .align(Alignment.TopEnd)
                    .padding(end = 2.dp, top = 2.dp)
                    .background(indicatorColor, CircleShape)
                    .background(brush = brush, CircleShape)
                    .border(2.4.dp, Color.White, CircleShape)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddItemSheet(
    mode: AddMode,
    date: LocalDate,
    onDismiss: () -> Unit,
    onSave: (AddItemCommand) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var time by remember { mutableStateOf(LocalTime.now()) }
    var isImportant by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = PanelBg,                    // ✅ Тёмный фон шита
        contentColor = Color.White,                  // ✅ Белый цвет по умолчанию
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = PanelBorder                  // ✅ Серая ручка, чтобы было видно
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = when (mode) {
                    AddMode.Event -> "Новое событие"
                    AddMode.Task -> "Новая задача"
                    AddMode.Note -> "Новая заметка"
                },
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White                  // ✅
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Дата: $date",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)  // ✅ Приглушённый белый
            )
            Spacer(Modifier.height(16.dp))

            // ✅ Кастомные цвета для текстового поля
            val textFieldColors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                cursorColor = Color.White
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Название") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = textFieldColors              // ✅
            )
            Spacer(Modifier.height(12.dp))

            if (mode == AddMode.Event) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isImportant,
                        onCheckedChange = { isImportant = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color.White,
                            uncheckedColor = Color.White.copy(alpha = 0.7f),
                            checkmarkColor = PanelBg
                        )
                    )
                    Text("Важное событие", color = Color.White)
                    Spacer(Modifier.height(12.dp))
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(if (mode == AddMode.Task) "Описание задачи" else "Описание") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (mode == AddMode.Event) 100.dp else 150.dp),
                maxLines = if (mode == AddMode.Event) 4 else 6,
                colors = textFieldColors              // ✅
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isSaving
                ) {
                    Text("Отмена", color = Color.White.copy(alpha = 0.7f))
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        isSaving = true
                        onSave(
                            AddItemCommand(
                                mode = mode,
                                date = date,
                                title = title.trim(),
                                description = description.trim(),
                                time = if (mode == AddMode.Event) time else null,
                                isImportant = if (mode == AddMode.Event) isImportant else false
                            )
                        )
                    },
                    enabled = title.isNotBlank() && !isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = PanelBg            // ✅ Инверсия: белая кнопка, тёмный текст
                    )
                ) {
                    Text("Сохранить")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayDetailsSheet(
    date: LocalDate,
    dayData: CalendarDay?,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = PanelBg,                    // ✅ Тёмный фон
        contentColor = Color.White,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = PanelBorder)
        }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                "Сводка за $date",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White                  // ✅
            )
            Spacer(Modifier.height(12.dp))
            dayData?.let { data ->
                Text(
                    "Событий: ${data.events.size}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "Задач: ${data.tasks.size} (выполнено: ${data.tasksCompleted})",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "Заметок: ${data.notes.size}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            } ?: Text(
                "Нет данных",
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

private fun findDayData(pages: List<CalendarPage>, date: LocalDate): CalendarDay? {
    return pages.flatMap { it.grid.flatten() }.firstOrNull { it?.date == date }
}

@Composable
private fun LoadingIndicator() = Box(
    Modifier.fillMaxSize(),
    Alignment.Center
) {
    CircularProgressIndicator()
}

@Composable
private fun ErrorView(onRetry: () -> Unit) = Box(
    Modifier.fillMaxSize(),
    Alignment.Center
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Ошибка загрузки")
        Button(onClick = onRetry) { Text("Повторить") }
    }
}