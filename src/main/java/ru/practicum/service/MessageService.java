package ru.practicum.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.practicum.BotMessageSender;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final BotMessageSender botMessageSender;
    private final MenuService menuService;


    public void sendSimpleMessage(Long chatId, String text) {
        botMessageSender.sendMessage(chatId, text);
    }

    public void sendMessageWithKeyboard(Long chatId, String text, InlineKeyboardMarkup keyboard) {
        botMessageSender.sendMessage(chatId, text, keyboard);
    }

    public void sendHelpMessage(Long chatId) {
        String helpText = """
                💡 *Как работает бот*:

                1. /record или кнопка "Записаться" — начать запись к врачу
                2. /my_records — посмотреть активные записи
                3. /cancel_record — отменить запись
                4. /menu — вернуться в главное меню
                5. /help — получить это сообщение
                
                Если у вас есть вопросы — напишите нам!
                """;
        botMessageSender.sendMessage(chatId, helpText, menuService.buildMainMenuKeyboard());

    }
}