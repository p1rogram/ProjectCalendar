package com.example.projectcalendar.domain.usecase.note

import com.example.projectcalendar.data.repository.NoteRepository
import com.example.projectcalendar.domain.model.Note
import javax.inject.Inject

class CreateNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(note: Note): Long {
        // Валидация входных данных
        require(note.content.isNotBlank()) { "Content cannot be blank" }

        // Сохранение и возврат ID
        return noteRepository.addNote(note)
    }
}