package com.example.assistant.bot;

import com.example.assistant.config.BotConfig;
import com.example.assistant.model.Note;
import com.example.assistant.service.NoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig config;
    private final NoteService noteService;

    @Override
    public String getBotUsername() {
        return config.getUsername();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleTextCommand(update);
        } else if (update.hasCallbackQuery()) {
            handleCallback(update);
        }
    }

    private void handleTextCommand(Update update) {
        String text = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

        if (text.equals("/start")) {
            sendTextMessage(chatId, "Привет! Я ассистент 🤖. Используй /note, /shownotes, /deletenote");
        } else if (text.startsWith("/note ")) {
            String noteText = text.substring(6).trim();
            if (!noteText.isEmpty()) {
                noteService.addNote(chatId, noteText);
                sendTextMessage(chatId, "Заметка сохранена ✅");
            } else {
                sendTextMessage(chatId, "⚠️ Нельзя сохранить пустую заметку.");
            }
        } else if (text.equals("/shownotes")) {
            List<Note> notes = noteService.getNotes(chatId);
            if (notes.isEmpty()) {
                sendTextMessage(chatId, "У тебя пока нет заметок.");
            } else {
                StringBuilder sb = new StringBuilder("📝 Твои заметки:\n\n");
                for (int i = 0; i < notes.size(); i++) {
                    sb.append(i).append(") ").append(notes.get(i)).append("\n");
                }
                sendTextMessage(chatId, sb.toString());
            }
        } else if (text.startsWith("/deletenote ")) {
            try {
                int index = Integer.parseInt(text.substring(12).trim());
                String callbackData = "DELETE_NOTE_" + index;
                String textPreview = String.valueOf(noteService.getNotes(chatId).get(index));

                SendMessage confirm = SendMessage.builder()
                        .chatId(String.valueOf(chatId))
                        .text("Удалить заметку \"" + textPreview + "\"?")
                        .replyMarkup(InlineKeyboardMarkup.builder()
                                .keyboard(List.of(
                                        List.of(InlineKeyboardButton.builder()
                                                .text("✅ Удалить")
                                                .callbackData(callbackData)
                                                .build())
                                ))
                                .build())
                        .build();

                execute(confirm);
            } catch (Exception e) {
                sendTextMessage(chatId, "⚠️ Неверный номер заметки.");
            }
        } else {
            sendTextMessage(chatId, "Неизвестная команда. Попробуй /start");
        }
    }

    private void handleCallback(Update update) {
        String data = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        if (data.startsWith("DELETE_NOTE_")) {
            int index = Integer.parseInt(data.replace("DELETE_NOTE_", ""));
            boolean deleted = noteService.deleteNote(chatId, index);
            if (deleted) {
                sendTextMessage(chatId, "Заметка удалена 🗑️");
            } else {
                sendTextMessage(chatId, "⚠️ Заметка не найдена.");
            }
        }
    }

    private void sendTextMessage(long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(text)
                .build();

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения", e);
        }
    }
}

