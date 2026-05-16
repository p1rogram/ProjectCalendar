package com.example.projectcalendar.di

import com.example.projectcalendar.data.db.Dao.EventDao
import com.example.projectcalendar.data.db.Dao.NoteDao
import com.example.projectcalendar.data.db.Dao.ReminderDao
import com.example.projectcalendar.data.db.Dao.TaskDao
import com.example.projectcalendar.data.repository.EventRepository
import com.example.projectcalendar.data.repository.NoteRepository
import com.example.projectcalendar.data.repository.ReminderRepository
import com.example.projectcalendar.data.repository.TaskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideEventRepository(eventDao: EventDao) : EventRepository {
        return EventRepository(eventDao)
    }

    @Singleton
    @Provides
    fun provideTaskRepository(taskDao: TaskDao) : TaskRepository {
        return TaskRepository(taskDao)
    }

    @Singleton
    @Provides
    fun provideNoteRepository(noteDao: NoteDao) : NoteRepository{
        return NoteRepository(noteDao)
    }

    @Singleton
    @Provides
    fun provideReminderRepository(reminderDao: ReminderDao) : ReminderRepository {
        return ReminderRepository(reminderDao)
    }

}