package com.example.projectcalendar.di

import android.content.Context
import androidx.room.Room
import com.example.projectcalendar.data.db.AppDatabase
import com.example.projectcalendar.data.db.Dao.EventDao
import com.example.projectcalendar.data.db.Dao.NoteDao
import com.example.projectcalendar.data.db.Dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideAppDatabase(
        @ApplicationContext context: Context  // ← Добавь параметр
    ): AppDatabase {
        return Room.databaseBuilder(  // ← Используй builder, а не просто AppDatabase
                context,
                AppDatabase::class.java,
                "calendar.db"  // ← Имя твоей БД
            ).fallbackToDestructiveMigration(false) // ← ДОБАВИТЬ ЭТУ СТРОКУ
            .build()
    }

    @Singleton
    @Provides
    fun provideEventDao(
        appDatabase: AppDatabase
    ): EventDao {
        return appDatabase.eventDao()
    }

    @Singleton
    @Provides
    fun provideTaskDao(
        appDatabase: AppDatabase
    ): TaskDao {
        return appDatabase.taskDao()
    }

    @Singleton
    @Provides
    fun provideNoteDao(
        appDatabase: AppDatabase
    ): NoteDao {
        return appDatabase.noteDao()
    }

}