package com.example.projectcalendar.domain.usecase.event

import com.example.projectcalendar.data.repository.EventRepository
import com.example.projectcalendar.data.repository.ReminderRepository
import com.example.projectcalendar.domain.model.Event
import com.example.projectcalendar.domain.model.Reminder

class CreateEventWithReminderUseCase(
    private val eventRepository: EventRepository,
    private val reminderRepository: ReminderRepository
) {


    suspend operator fun invoke(event: Event, reminders: List<Reminder>): Long {
        require(event.isValid()) { "Ошибка валидации: событие содержит пустые обязательные поля" }

        val newEventId = eventRepository.addEvent(event)

        val preparedReminders = reminders.map { reminder ->
            reminder.copy(
                eventId = newEventId,
                notificationId = 0L,
                isTriggered = false,
                triggeredAt = null
            )
        }

        preparedReminders.forEach { reminderRepository.addReminder(it) }

        return newEventId
    }
}