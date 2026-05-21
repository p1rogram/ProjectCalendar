package com.example.projectcalendar.domain.usecase.event

import com.example.projectcalendar.data.repository.EventRepository
import com.example.projectcalendar.domain.model.Event

class CreateEventUseCase(
    private val eventRepository: EventRepository,
) {


    suspend operator fun invoke(event: Event): Long {
        require(event.isValid()) { "Ошибка валидации: событие содержит пустые обязательные поля" }
        val newEventId = eventRepository.addEvent(event)
        return newEventId
    }
}