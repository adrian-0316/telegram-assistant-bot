package com.example.assistant.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NoteService {
    private final Map<Long, List<String>> userNotes = new HashMap<>();

    public void addNote(Long chatId, String note) {
        userNotes.computeIfAbsent(chatId, k -> new ArrayList<>()).add(note);
    }

    public List<String> getNotes(Long chatId) {
        return userNotes.getOrDefault(chatId, Collections.emptyList());
    }

    public boolean deleteNote(Long chatId, int index) {
        List<String> notes = userNotes.get(chatId);
        if (notes != null && index >= 0 && index < notes.size()) {
            notes.remove(index);
            return true;
        }
        return false;
    }
}
