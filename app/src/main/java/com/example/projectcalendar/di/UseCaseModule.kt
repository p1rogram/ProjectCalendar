package com.example.projectcalendar.di

import com.example.projectcalendar.data.repository.EventRepository
import com.example.projectcalendar.data.repository.NoteRepository
import com.example.projectcalendar.data.repository.TaskRepository
import com.example.projectcalendar.domain.usecase.calendar.GetCalendarMonthUseCase
import com.example.projectcalendar.domain.usecase.calendar.GetCalendarUseCase
import com.example.projectcalendar.domain.usecase.event.CreateEventUseCase
import com.example.projectcalendar.domain.usecase.event.DeleteEventUseCase
import com.example.projectcalendar.domain.usecase.note.CreateNoteUseCase
import com.example.projectcalendar.domain.usecase.note.GetNotesForDateUseCase
import com.example.projectcalendar.domain.usecase.task.CreateTaskUseCase
import com.example.projectcalendar.domain.usecase.task.GetTasksForDateUseCase
import com.example.projectcalendar.domain.usecase.task.ToggleTaskCompletionUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @ViewModelScoped
    @Provides
    fun provideGetCalendarUseCase(
        eventRepo: EventRepository,
        taskRepo: TaskRepository,
        noteRepo: NoteRepository
    ): GetCalendarUseCase {
        return GetCalendarUseCase(eventRepo,taskRepo,noteRepo)
    }


    @ViewModelScoped
    @Provides
    fun provideCreateEventUseCase(
        eventRepo: EventRepository,
    ): CreateEventUseCase {
        return CreateEventUseCase(eventRepo)
    }


    @ViewModelScoped
    @Provides
    fun provideToggleTaskCompletionUseCase(
        taskRepo: TaskRepository
    ): ToggleTaskCompletionUseCase {
        return ToggleTaskCompletionUseCase(taskRepo)
    }


    @ViewModelScoped
    @Provides
    fun provideDeleteEventUseCase(
        eventRepo: EventRepository,
    ): DeleteEventUseCase {
        return DeleteEventUseCase(eventRepo)
    }
    @ViewModelScoped
    @Provides
    fun provideGetTasksForDateUseCase(
        taskRepo: TaskRepository
    ) : GetTasksForDateUseCase {
        return GetTasksForDateUseCase(taskRepo)
    }


    @ViewModelScoped
    @Provides
    fun provideGetNotesForDateUseCase(
        noteRepo: NoteRepository
    ) : GetNotesForDateUseCase {
        return GetNotesForDateUseCase(noteRepo)
    }



    @ViewModelScoped  // ← Для каждого метода
    @Provides         // ← Для каждого метода
    fun provideGetCalendarMonthUseCase(
        eventRepo: EventRepository,
        taskRepo: TaskRepository,
        noteRepo: NoteRepository
    ): GetCalendarMonthUseCase {
        return GetCalendarMonthUseCase(eventRepo, taskRepo, noteRepo)
    }

    @ViewModelScoped
    @Provides
    fun provideCreateTaskUseCase(
        taskRepo: TaskRepository
    ): CreateTaskUseCase {
        return CreateTaskUseCase(taskRepo)
    }

    @ViewModelScoped
    @Provides
    fun provideCreateNoteUseCase(
        noteRepo: NoteRepository
    ): CreateNoteUseCase {
        return CreateNoteUseCase(noteRepo)
    }

}