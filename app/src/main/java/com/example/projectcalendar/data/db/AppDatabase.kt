package com.example.projectcalendar.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.projectcalendar.data.db.Converter.Converters
import com.example.projectcalendar.data.db.Dao.EventDao
import com.example.projectcalendar.data.db.Dao.NoteDao
import com.example.projectcalendar.data.db.Dao.TaskDao
import com.example.projectcalendar.data.db.Entity.EventEntity
import com.example.projectcalendar.data.db.Entity.NoteEntity
import com.example.projectcalendar.data.db.Entity.TaskEntity

@Database(
    entities = [
        EventEntity::class,
        TaskEntity::class,
        NoteEntity::class,
    ],
    version = 1 ,
    exportSchema = true

)

@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // Абстрактные методы для получения DAO
    abstract fun eventDao(): EventDao
    abstract fun taskDao(): TaskDao
    abstract fun noteDao(): NoteDao
}