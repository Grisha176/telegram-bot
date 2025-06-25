package ru.practicum;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public interface BotMessageSender {
    void sendMessage(Long chatId, String text, InlineKeyboardMarkup keyboard);
    void sendMessage(Long chatId, String text);
}