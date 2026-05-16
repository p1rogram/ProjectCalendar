package com.example.projectcalendar.domain.usecase.note

import com.example.projectcalendar.data.repository.NoteRepository
import com.example.projectcalendar.domain.model.Note
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class GetNotesForDateUseCase(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(date: LocalDate): List<Note> =
        noteRepository.getNotesForDate(date).first()
}