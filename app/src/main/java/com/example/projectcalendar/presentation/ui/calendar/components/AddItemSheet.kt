package com.example.projectcalendar.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.projectcalendar.presentation.ui.common.AddMode
import java.time.LocalDate
import java.time.LocalTime

// Команда для передачи данных в ViewModel
data class AddItemCommand(
    val mode: AddMode,
    val date: LocalDate,
    val title: String,
    val description: String,
    val time: LocalTime? = null // Нужно для Event и Reminder :TODO(REFRESH)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemSheet(
    mode: AddMode,
    date: LocalDate,
    onDismiss: () -> Unit,
    onSave: (AddItemCommand) -> Unit
) {
    // Локальное состояние полей ввода (живёт только пока открыт шит)
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var time by remember { mutableStateOf(LocalTime.now()) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Заголовок
            Text(
                text = when (mode) {
                    AddMode.Event -> "Новое событие"
                    AddMode.Task -> "Новая задача"
                    AddMode.Note -> "Новая заметка"
                },
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(8.dp))
            Text("Дата: $date", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(16.dp))

            // Общее поле: Название
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Название") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            // Динамические поля в зависимости от режима
            when (mode) {
                AddMode.Event -> {
                    OutlinedTextField(
                        value = time.toString(),
                        onValueChange = {}, // TODO: подключить TimePicker
                        label = { Text("Время начала") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        readOnly = true
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Описание") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        maxLines = 4
                    )
                }
                AddMode.Task -> {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Текст заметки") },
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        maxLines = 6
                    )
                }
                AddMode.Note -> {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Текст заметки") },
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        maxLines = 6
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Кнопки действий
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Отмена")
                }
                Spacer(Modifier.width(8.dp))
                FilledButton(
                    onClick = {
                        onSave(AddItemCommand(mode, date, title.trim(), description.trim(), time))
                    },
                    enabled = title.isNotBlank()
                ) {
                    Text("Сохранить")
                }
            }
        }
    }
}

@Composable
fun FilledButton(onClick: () -> Unit, enabled: Boolean, content: @Composable () -> Unit) {
    TODO("Not yet implemented")
}