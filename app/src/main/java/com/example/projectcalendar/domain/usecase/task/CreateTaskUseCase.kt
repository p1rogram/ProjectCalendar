package com.example.projectcalendar.domain.usecase.task

import com.example.projectcalendar.data.repository.TaskRepository
import com.example.projectcalendar.domain.model.Task

class CreateTaskUseCase(
    private val taskRepository: TaskRepository
) {
////////////////РАЗБОР
    suspend operator fun invoke(task: Task): Long {
        // Валидация
        require(task.title.isNotBlank()) { "Заголовок задачи не может быть пустым" }

        // Сохраняем и возвращаем ID
        return taskRepository.addTask(task)
    }
}