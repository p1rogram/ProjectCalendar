package com.example.projectcalendar.di

import com.example.projectcalendar.data.repository.EventRepository
import com.example.projectcalendar.data.repository.NoteRepository
import com.example.projectcalendar.data.repository.ReminderRepository
import com.example.projectcalendar.data.repository.TaskRepository
import com.example.projectcalendar.domain.usecase.calendar.GetCalendarUseCase
import com.example.projectcalendar.domain.usecase.event.CreateEventWithReminderUseCase
import com.example.projectcalendar.domain.usecase.event.DeleteEventUseCase
import com.example.projectcalendar.domain.usecase.note.GetNotesForDateUseCase
import com.example.projectcalendar.domain.usecase.reminder.GetPendingRemindersUseCase
import com.example.projectcalendar.domain.usecase.reminder.MarkReminderTriggeredUseCase
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
    fun provideCreateEventWithReminderUseCase(
        eventRepo: EventRepository,
        reminderRepo: ReminderRepository
    ): CreateEventWithReminderUseCase {
        return CreateEventWithReminderUseCase(eventRepo,reminderRepo)
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
        reminderRepo: ReminderRepository
    ): DeleteEventUseCase {
        return DeleteEventUseCase(eventRepo,reminderRepo)
    }

    @ViewModelScoped
    @Provides
    fun provideGetPendingRemindersUseCase(
        reminderRepo: ReminderRepository
    ): GetPendingRemindersUseCase {
        return GetPendingRemindersUseCase(reminderRepo)
    }

    @ViewModelScoped
    @Provides
    fun provideMarkReminderAsDoneUseCase(
        reminderRepo: ReminderRepository
    ): MarkReminderTriggeredUseCase {
        return MarkReminderTriggeredUseCase(reminderRepo)
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

}