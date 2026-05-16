package com.example.projectcalendar.data.db.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.projectcalendar.data.db.Entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE event_id = :eventId")
    fun getRemindersForEvent(eventId: Long): Flow<List<ReminderEntity>> // Убрал suspend, добавил Flow


    @Query("""
    SELECT r.* FROM reminders r
    JOIN events e ON r.event_id = e.id
    WHERE r.is_triggered = 0
      AND (e.startDateTime - r.offset_minutes * 60000) <= :currentTimeMillis
    ORDER BY (e.startDateTime - r.offset_minutes * 60000) ASC
""")
    suspend fun getPendingReminders(currentTimeMillis: Long = System.currentTimeMillis()): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE is_triggered = 1")
    suspend fun getTriggeredReminders(): List<ReminderEntity>

    @Query("UPDATE reminders SET is_triggered = 1, triggered_at = :triggeredMillis WHERE id = :reminderId")
    suspend fun markAsTriggered(reminderId: Long, triggeredMillis: Long = System.currentTimeMillis())

    @Query("UPDATE reminders SET is_triggered = 0, triggered_at = NULL WHERE id = :reminderId")
    suspend fun resetTriggered(reminderId: Long)
    @Query("DELETE FROM reminders WHERE event_id = :eventId")
    suspend fun deleteRemindersByEventId(eventId: Long)
    @Query("SELECT COUNT(*) FROM reminders WHERE is_triggered = 0")
    suspend fun getPendingRemindersCount(): Int

}