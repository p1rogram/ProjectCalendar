package com.example.projectcalendar.domain.usecase.reminder

import com.example.projectcalendar.data.repository.ReminderRepository

class MarkReminderTriggeredUseCase(
    private val reminderRepository: ReminderRepository

) {
    suspend operator fun invoke(reminderId: Long) =reminderRepository.markAsTriggered(reminderId)

}