package com.example.projectcalendar.data.db.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.projectcalendar.data.db.Entity.TaskEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE date = :dateMillis ORDER BY priority DESC")
    fun getTasksByDate(dateMillis: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE is_completed = 0 ORDER BY date ASC")
    fun getIncompleteTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE is_completed = 1 ORDER BY completed_at DESC")
    fun getCompletedTasks(): Flow<List<TaskEntity>>

    @Query("UPDATE tasks SET is_completed = 1, completed_at = :completedMillis WHERE id = :taskId")
    suspend fun completeTask(taskId: Long, completedMillis: Long = System.currentTimeMillis())

    @Query("UPDATE tasks SET is_completed = 0, completed_at = NULL WHERE id = :taskId")
    suspend fun uncompleteTask(taskId: Long)

    @Query("SELECT COUNT(*) FROM tasks WHERE is_completed = 1")
    suspend fun getCompletedCount(): Int

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun getTotalTasksCount(): Int
}