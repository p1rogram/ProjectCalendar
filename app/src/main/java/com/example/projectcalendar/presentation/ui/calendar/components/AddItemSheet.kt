package com.example.projectcalendar.presentation.ui.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.projectcalendar.presentation.viewmodel.ItemType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
/////////РАЗОБРАТЬ
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemSheet(
    selectedDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (ItemType, String, String?) -> Unit
) {
    var selectedType by remember { mutableStateOf<ItemType>(ItemType.Task) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Добавить на ${selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(16.dp))

            // Выбор типа
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedType == ItemType.Task,
                    onClick = { selectedType = ItemType.Task },
                    label = { Text("Задача") }
                )
                FilterChip(
                    selected = selectedType == ItemType.Note,
                    onClick = { selectedType = ItemType.Note },
                    label = { Text("Заметка") }
                )
                FilterChip(
                    selected = selectedType == ItemType.Reminder,
                    onClick = { selectedType = ItemType.Reminder },
                    label = { Text("Напоминание") }
                )
            }

            Spacer(Modifier.height(16.dp))

            // Поле заголовка
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Заголовок") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            // Поле содержимого (для заметок и описания)
            if (selectedType == ItemType.Note || selectedType == ItemType.Reminder) {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Содержимое") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )
                Spacer(Modifier.height(8.dp))
            }

            // Кнопки
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text("Отмена")
                }

                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onConfirm(selectedType, title, content.ifBlank { null })
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = title.isNotBlank()
                ) {
                    Text("Сохранить")
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}