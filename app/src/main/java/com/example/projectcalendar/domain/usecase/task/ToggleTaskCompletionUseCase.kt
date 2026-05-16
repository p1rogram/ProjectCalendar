package com.example.projectcalendar.domain.usecase.task

import com.example.projectcalendar.data.repository.TaskRepository

class ToggleTaskCompletionUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long) : Unit {
        val task = taskRepository.getTaskById(taskId)
        require(task != null) { "Task not found" }
        val updatedTask = when(task.isCompleted){
            true -> task.markIncomplete()
            false -> task.markComplete()
        }
        return taskRepository.updateTask(updatedTask)
    }
}