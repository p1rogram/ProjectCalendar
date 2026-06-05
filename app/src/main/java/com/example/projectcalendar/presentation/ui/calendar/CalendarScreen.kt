package com.example.projectcalendar.presentation.ui.screen
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.border
import androidx.compose.ui.window.DialogProperties
import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush.Companion.radialGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.projectcalendar.domain.model.CalendarDay
import com.example.projectcalendar.domain.model.Event
import com.example.projectcalendar.domain.model.Note
import com.example.projectcalendar.domain.model.Task
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
import androidx.core.graphics.drawable.toDrawable

private val ScreenBackground = GrayBack
private val week: List<String> = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
private val brush = radialGradient(
    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.2f))
)

private val PanelShape = RoundedCornerShape(20.dp)
private val PanelBorder = Color(0xFF9A9A9A)
private val PanelBg = Color(0xFF1A1A1A)

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
        window?.setBackgroundDrawable(ScreenBackground.toArgb().toDrawable())
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
                        beyondViewportPageCount = 1
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
                    onDismiss = viewModel::closeSheets,
                    onReorderEvent = viewModel::reorderEvent,
                    onReorderTask = viewModel::reorderTask,
                    onReorderNote = viewModel::reorderNote
                )
            }
        }
    }
}

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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onPrevClick
                )
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("←", color = Color.White, fontSize = 20.sp)
        }

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

        Box(
            modifier = Modifier
                .weight(1f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onNextClick
                )
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("→", color = Color.White, fontSize = 20.sp)
        }
    }
}

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
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .border(2.dp, PanelBorder, PanelShape)
                .clip(PanelShape)
                .background(PanelBg)
                .background(brush = brush)
        ) {
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
                containerColor = PanelBg
            ) {
                DropdownMenuItem(
                    text = { Text("Событие", color = Color.White) },
                    onClick = { onAddClick(AddMode.Event); showMenu = false }
                )
                DropdownMenuItem(
                    text = { Text("Задача", color = Color.White) },
                    onClick = { onAddClick(AddMode.Task); showMenu = false }
                )
                DropdownMenuItem(
                    text = { Text("Заметка", color = Color.White) },
                    onClick = { onAddClick(AddMode.Note); showMenu = false }
                )
            }
        }

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

        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .border(2.dp, PanelBorder, PanelShape)
                .clip(PanelShape)
                .background(PanelBg)
                .clickable { /* TODO: настройки */ }
                .background(brush = brush),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = "Настройки",
                tint = Color.White
            )
        }
    }
}

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

    val indicatorColor = when {
        day == null -> null
        day.hasUnimportantEvent -> Color.Red
        day.hasTasksOrNotes -> Color.Yellow
        else -> null
    }

    Box {
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
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var isImportant by rememberSaveable { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    var startTime by rememberSaveable { mutableStateOf(LocalTime.of(9, 0)) }
    var endTime by rememberSaveable { mutableStateOf(LocalTime.of(10, 0)) }
    var isAllDay by rememberSaveable { mutableStateOf(false) }
    var isReminder by rememberSaveable { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = PanelBg,
        contentColor = Color.White,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = PanelBorder)
        }
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .heightIn(min = 500.dp)  // ✅ Фиксируем минимальную высоту
                .padding(16.dp)
                .imePadding()
                .animateContentSize()  // ✅ Плавная анимация изменения размера
        ) {
            Text(
                text = when (mode) {
                    AddMode.Event -> "Новое событие"
                    AddMode.Task -> "Новая задача"
                    AddMode.Note -> "Новая заметка"
                },
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Дата: $date",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(16.dp))

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
                colors = textFieldColors
            )
            Spacer(Modifier.height(12.dp))

            if (mode == AddMode.Event) {
                // Чекбокс "Весь день"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isAllDay,
                        onCheckedChange = {
                            isAllDay = it
                            if (it) isReminder = false
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color.White,
                            uncheckedColor = Color.White.copy(alpha = 0.7f),
                            checkmarkColor = PanelBg
                        )
                    )
                    Text("Весь день", color = Color.White)
                }

                // Чекбокс "Напоминание"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isReminder,
                        onCheckedChange = {
                            isReminder = it
                            if (it) isAllDay = false
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color.White,
                            uncheckedColor = Color.White.copy(alpha = 0.7f),
                            checkmarkColor = PanelBg
                        )
                    )
                    Text("Разовое напоминание", color = Color.White)
                }

                // ✅ Поля времени с фиксированной высотой (даже когда скрыты)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)  // ✅ Фиксируем высоту блока времени
                ) {
                    if (!isAllDay) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TimePickerField(
                                label = "Начало",
                                time = startTime,
                                onClick = { showStartTimePicker = true },
                                modifier = Modifier.weight(1f)
                            )

                            if (!isReminder) {
                                TimePickerField(
                                    label = "Конец",
                                    time = endTime,
                                    onClick = { showEndTimePicker = true },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Чекбокс "Важное событие"
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
                }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(if (mode == AddMode.Task) "Описание задачи" else if (mode == AddMode.Note) "Содержание" else "Описание") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (mode == AddMode.Event) 100.dp else 150.dp),
                maxLines = if (mode == AddMode.Event) 4 else 6,
                colors = textFieldColors
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss, enabled = !isSaving) {
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
                                time = null,
                                startTime = if (mode == AddMode.Event && !isAllDay) startTime else null,
                                endTime = if (mode == AddMode.Event && !isAllDay && !isReminder) endTime else null,
                                isAllDay = if (mode == AddMode.Event) isAllDay else false,
                                isReminder = if (mode == AddMode.Event) isReminder else false,
                                isImportant = if (mode == AddMode.Event) isImportant else false
                            )
                        )
                    },
                    enabled = title.isNotBlank() && !isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = PanelBg
                    )
                ) {
                    Text("Сохранить")
                }
            }
        }
    }

    // Time Pickers
    if (showStartTimePicker) {
        TimePickerDialog(
            currentTime = startTime,
            onDismiss = { showStartTimePicker = false },
            onConfirm = { time ->
                startTime = time
                showStartTimePicker = false
                if (endTime.isBefore(startTime)) {
                    endTime = startTime.plusHours(1)
                }
            }
        )
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            currentTime = endTime,
            onDismiss = { showEndTimePicker = false },
            onConfirm = { time ->
                endTime = time
                showEndTimePicker = false
            }
        )
    }
}

// Компонент поля выбора времени
@Composable
private fun TimePickerField(
    label: String,
    time: LocalTime,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.5f),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = time.toString(),
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Диалог выбора времени

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    currentTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.hour,
        initialMinute = currentTime.minute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PanelBg,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(LocalTime.of(timePickerState.hour, timePickerState.minute))
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.White.copy(alpha = 0.7f)
                )
            ) {
                Text("Отмена")
            }
        },
        title = {
            Text(
                "Выберите время",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = Color.White.copy(alpha = 0.1f),
                        clockDialSelectedContentColor = Color.White,
                        clockDialUnselectedContentColor = Color.White.copy(alpha = 0.5f),
                        selectorColor = Color.White,
                        timeSelectorSelectedContainerColor = Color.White.copy(alpha = 0.2f),
                        timeSelectorSelectedContentColor = Color.White,
                        timeSelectorUnselectedContainerColor = Color.Transparent,
                        timeSelectorUnselectedContentColor = Color.White.copy(alpha = 0.7f),
                        periodSelectorBorderColor = Color.White.copy(alpha = 0.3f),
                        periodSelectorSelectedContainerColor = Color.White.copy(alpha = 0.2f),
                        periodSelectorSelectedContentColor = Color.White,
                        periodSelectorUnselectedContainerColor = Color.Transparent,
                        periodSelectorUnselectedContentColor = Color.White.copy(alpha = 0.7f)
                    )
                )
            }
        }
    )
}
// ==========================================
// 🔥 НОВАЯ СВОДКА С LAZY COLUMN
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayDetailsSheet(
    date: LocalDate,
    dayData: CalendarDay?,
    onDismiss: () -> Unit,
    onReorderEvent: (Int, Int) -> Unit,
    onReorderTask: (Int, Int) -> Unit,
    onReorderNote: (Int, Int) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = PanelBg,
        contentColor = Color.White,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = PanelBorder)
        }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .fillMaxWidth()
        ) {
            Text(
                "Сводка за $date",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            Spacer(Modifier.height(16.dp))

            if (dayData == null || (dayData.events.isEmpty() && dayData.tasks.isEmpty() && dayData.notes.isEmpty())) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Нет данных",
                        color = Color.White.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // === СЕКЦИЯ: СОБЫТИЯ ===
                    if (dayData.events.isNotEmpty()) {
                        item {
                            SectionHeader("События", dayData.events.size)
                        }
                        itemsIndexed(
                            items = dayData.events,
                            key = { index, event -> "event_${event.id ?: index}" }
                        ) { index, event ->
                            EventItem(
                                event = event,
                                index = index,
                                totalCount = dayData.events.size,
                                onMoveUp = { if (index > 0) onReorderEvent(index, index - 1) },
                                onMoveDown = { if (index < dayData.events.size - 1) onReorderEvent(index, index + 1) }
                            )
                        }
                    }

                    // === СЕКЦИЯ: ЗАДАЧИ ===
                    if (dayData.tasks.isNotEmpty()) {
                        item {
                            SectionHeader("Задачи", dayData.tasks.size)
                        }
                        itemsIndexed(
                            items = dayData.tasks,
                            key = { index, task -> "task_${task.id ?: index}" }
                        ) { index, task ->
                            TaskItem(
                                task = task,
                                index = index,
                                totalCount = dayData.tasks.size,
                                onMoveUp = { if (index > 0) onReorderTask(index, index - 1) },
                                onMoveDown = { if (index < dayData.tasks.size - 1) onReorderTask(index, index + 1) }
                            )
                        }
                    }

                    // === СЕКЦИЯ: ЗАМЕТКИ ===
                    if (dayData.notes.isNotEmpty()) {
                        item {
                            SectionHeader("Заметки", dayData.notes.size)
                        }
                        itemsIndexed(
                            items = dayData.notes,
                            key = { index, note -> "note_${note.id ?: index}" }
                        ) { index, note ->
                            NoteItem(
                                note = note,
                                index = index,
                                totalCount = dayData.notes.size,
                                onMoveUp = { if (index > 0) onReorderNote(index, index - 1) },
                                onMoveDown = { if (index < dayData.notes.size - 1) onReorderNote(index, index + 1) }
                            )
                        }
                    }

                    item {
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun EventItem(
    event: Event,
    index: Int,
    totalCount: Int,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Заголовок события
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.title,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = buildString {
                            append(if (event.isImportant) "⭐ Важное" else "Обычное")
                            append(" • ")
                            when {
                                event.isAllDay -> append("Весь день")
                                event.isReminder -> append("🔔 ${event.startDateTime.toLocalTime()}")
                                else -> append("${event.startDateTime.toLocalTime()} - ${event.startDateTime.toLocalTime().plusHours(1)}")
                            }
                        },
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }
            }

            // Детали (раскрываются)
            if (isExpanded && !event.description.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = event.description,
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Кнопки управления
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onMoveUp,
                    enabled = index > 0,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Вверх",
                        tint = if (index > 0) Color.White else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onMoveDown,
                    enabled = index < totalCount - 1,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Вниз",
                        tint = if (index < totalCount - 1) Color.White else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskItem(
    task: Task,
    index: Int,
    totalCount: Int,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (task.isCompleted) Color.Green.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                    Text(
                        text = buildString {
                            append(when (task.priority) {
                                com.example.projectcalendar.domain.model.type.Priority.HIGH -> "🔴 Высокий"
                                com.example.projectcalendar.domain.model.type.Priority.MEDIUM -> "🟡 Средний"
                                com.example.projectcalendar.domain.model.type.Priority.LOW -> "🟢 Низкий"
                            })
                            append(" • ")
                            append(if (task.isCompleted) "✓ Выполнено" else "⏳ Не выполнено")
                        },
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }
            }

            if (isExpanded && !task.description.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = task.description,
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onMoveUp,
                    enabled = index > 0,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Вверх",
                        tint = if (index > 0) Color.White else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onMoveDown,
                    enabled = index < totalCount - 1,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Вниз",
                        tint = if (index < totalCount - 1) Color.White else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NoteItem(
    note: Note,
    index: Int,
    totalCount: Int,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Create,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    // ✅ ИСПРАВЛЕНО: показываем content заметки как заголовок
                    Text(
                        text = note.title,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Создана: ${note.createdAt.toLocalDate()}",
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }
            }
            // Полный текст заметки при раскрытии
            if (isExpanded && note.content.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = note.content,
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onMoveUp,
                    enabled = index > 0,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Вверх",
                        tint = if (index > 0) Color.White else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onMoveDown,
                    enabled = index < totalCount - 1,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Вниз",
                        tint = if (index < totalCount - 1) Color.White else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Bold
        )
        Box(
            modifier = Modifier
                .background(PanelBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = count.toString(),
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// ==========================================
// УТИЛИТЫ
// ==========================================

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