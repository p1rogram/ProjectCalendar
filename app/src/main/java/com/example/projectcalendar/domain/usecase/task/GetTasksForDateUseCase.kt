package com.example.projectcalendar.domain.usecase.task

import com.example.projectcalendar.data.repository.TaskRepository
import com.example.projectcalendar.domain.model.Task
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class GetTasksForDateUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(date: LocalDate): List<Task> =
        taskRepository.getTasksForDate(date).first()
}