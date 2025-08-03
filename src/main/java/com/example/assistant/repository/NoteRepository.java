package com.example.assistant.repository;



import com.example.assistant.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findAllByChatId(Long chatId);
}
