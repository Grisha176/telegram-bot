package ru.practicum.config;


import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.practicum.DoctorAppointmentBot;

@Configuration
public class TelegramBotConfig implements CommandLineRunner {

    @Autowired
    private DoctorAppointmentBot doctorBot;

    @Override
    public void run(String... args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(doctorBot);
        } catch (TelegramApiException e) {
            if (!e.getMessage().contains("Not Found")) {
                throw new RuntimeException("❌ Ошибка при регистрации бота", e);
            } else {
                System.out.println("⚠️ Вебхук не найден — это нормально, если вы используете Long Polling");
            }
        }
    }
}
