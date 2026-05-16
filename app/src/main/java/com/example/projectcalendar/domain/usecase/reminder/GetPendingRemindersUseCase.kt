package com.example.projectcalendar.domain.usecase.reminder

import com.example.projectcalendar.data.repository.ReminderRepository
import com.example.projectcalendar.domain.model.Reminder

class GetPendingRemindersUseCase(
    private val reminderRepository: ReminderRepository
) {
    suspend operator fun invoke(): List<Reminder> {
        return reminderRepository.getPendingReminders()
    }
}