package com.example.projectcalendar.domain.usecase.event

import com.example.projectcalendar.data.repository.EventRepository

class DeleteEventUseCase(
    private val eventRepository: EventRepository,
) {
    suspend operator fun invoke(eventId: Long) {
        val event = eventRepository.getEventById(eventId)
        require(event != null) { "Event not found with id: $eventId" }
        eventRepository.deleteEvent(event)
    }
}