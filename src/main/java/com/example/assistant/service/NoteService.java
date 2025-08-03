package com.example.assistant.service;

import com.example.assistant.model.Note;
import com.example.assistant.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;

    public void addNote(Long chatId, String text) {
        Note note = Note.builder()
                .chatId(chatId)
                .text(text)
                .build();
        noteRepository.save(note);
    }

    public List<Note> getNotes(Long chatId) {
        return noteRepository.findAllByChatId(chatId);
    }

    public boolean deleteNote(Long chatId, int index) {
        List<Note> notes = noteRepository.findAllByChatId(chatId);
        if (index >= 0 && index < notes.size()) {
            noteRepository.delete(notes.get(index));
            return true;
        }
        return false;
    }
}
