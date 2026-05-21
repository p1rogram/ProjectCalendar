    package com.example.projectcalendar.data.repository

    import com.example.projectcalendar.data.db.Dao.TaskDao
    import com.example.projectcalendar.data.db.Entity.TaskEntity
    import com.example.projectcalendar.data.utils.toEpochMillis
    import com.example.projectcalendar.data.utils.toLocalDate
    import com.example.projectcalendar.data.utils.toLocalDateTime
    import com.example.projectcalendar.data.utils.toStartOfDayMillis
    import com.example.projectcalendar.domain.model.Task
    import com.example.projectcalendar.domain.model.type.Priority
    import kotlinx.coroutines.flow.Flow
    import kotlinx.coroutines.flow.map
    import java.time.LocalDate

    class TaskRepository(
        private val taskDao: TaskDao
    ) {

        // ==========================================
        // 1. МАППЕРЫ (Entity ↔ Domain)
        // ==========================================
        private fun TaskEntity.toDomain(): Task = Task(
            id = id.takeIf { it > 0 },
            title = title,
            description = description?.takeIf { it.isNotBlank() },
            date = date.toLocalDate(),
            isCompleted = isCompleted,
            completedAt = completedAt?.toLocalDateTime(),
            priority = runCatching { Priority.valueOf(priority) }
                .getOrDefault(Priority.MEDIUM),
            createdAt = createdAt.toLocalDateTime(),
            updatedAt = updatedAt?.toLocalDateTime()
        )

        private fun Task.toEntity(): TaskEntity = TaskEntity(
            id = id ?: 0L,
            title = title,
            description = description ?: "",
            date = date.toStartOfDayMillis(),
            isCompleted = isCompleted,
            completedAt = completedAt?.toEpochMillis(),
            priority = priority.name,
            createdAt = createdAt.toEpochMillis(),
            updatedAt = updatedAt?.toEpochMillis()
        )

        // ==========================================
        // 2. ПУБЛИЧНОЕ API
        // ==========================================

        /** Поток задач на конкретный день */
        fun getTasksForDate(date: LocalDate): Flow<List<Task>> =
            taskDao.getTasksByDate(date.toStartOfDayMillis())
                .map { entities -> entities.map { it.toDomain() } }

        suspend fun getTasksForDateRange(start: LocalDate, end: LocalDate): Flow<List<Task>> =
            taskDao.getTasksByDateRange(start.toStartOfDayMillis(), end.toStartOfDayMillis())
                .map { entities -> entities.map { it.toDomain() } }

        /** Разовое получение задачи по ID */
        suspend fun getTaskById(id: Long): Task? =
            taskDao.getTaskById(id)?.toDomain()

        /** Поток невыполненных задач */
        fun getIncompleteTasks(): Flow<List<Task>> =
            taskDao.getIncompleteTasks()
                .map { entities -> entities.map { it.toDomain() } }

        /** Сохранить новую задачу */
        suspend fun addTask(task: Task): Long =
            taskDao.insertTask(task.toEntity())

        /** Обновить существующую задачу */
        suspend fun updateTask(task: Task) =
            taskDao.updateTask(task.toEntity())

        /** Удалить задачу */
        suspend fun deleteTask(task: Task) =
            taskDao.deleteTask(task.toEntity())

        /** Отметить задачу выполненной */
        suspend fun completeTask(id: Long, completedAt: Long = System.currentTimeMillis()) =
            taskDao.completeTask(id, completedAt)
    }