package ru.practicum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.practicum.model.Appointment;
import ru.practicum.model.Doctor;
import ru.practicum.service.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static ru.practicum.CallbackParser.DATE_TIME_FORMATTER;

@Component
public class DoctorAppointmentBot extends TelegramLongPollingBot implements BotMessageSender {

    private final String botToken;
    private final String botUsername;

    @Autowired
    private UserService userService;
    @Autowired
    @Lazy
    private NavigationService navigationService;
    @Autowired
    @Lazy
    private AppointmentService appointmentService;
    @Autowired
    @Lazy
    private MessageService messageService;
    @Autowired
    private DoctorService doctorService;

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public DoctorAppointmentBot(
            @Value("${telegram.bot.username}") String botUsername,
            @Value("${telegram.bot.token}") String botToken) {
        this.botToken = botToken;
        this.botUsername = botUsername;

    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        Long chatId;

        // 🔹 Обработка команд (/start, /record, /my_records)
        if (update.hasMessage() && update.getMessage().isCommand()) {
            Message message = update.getMessage();
            chatId = message.getChatId();
            String command = message.getText();

            switch (command) {
                case "/start", "/menu" -> navigationService.goToMainMenu(chatId);
                case "/record" -> navigationService.goToSpecializationMenu(chatId);
                case "/my_records" -> appointmentService.sendMyRecords(chatId);
                case "/cancel_record" -> appointmentService.sendCancelMenu(chatId);
                case "/help" -> messageService.sendHelpMessage(chatId);
                default -> messageService.sendSimpleMessage(chatId, "❌ Неизвестная команда");
            }

            return;
        }
        // 🔹 Обработка inline-кнопок (callbackData)
        if (update.hasCallbackQuery()) {
            CallbackQuery query = update.getCallbackQuery();
            chatId = query.getMessage().getChatId();
            String data = query.getData();

            switch (data) {
                case "record" -> navigationService.goToSpecializationMenu(chatId);
                case "my_records" -> appointmentService.sendMyRecords(chatId);
                case "cancel_record" -> appointmentService.sendCancelMenu(chatId);
                case "help" -> messageService.sendHelpMessage(chatId);
                case "main_menu" -> navigationService.goToMainMenu(chatId);
                case "archive_records" -> appointmentService.sendArchiveRecords(chatId);

                default -> {
                    // Если это не прямая команда — проверяем тип callbackData
                    if (data.startsWith("specialization_")) {
                        String specializationCode = data.replace("specialization_", "");
                        navigationService.goToDoctorMenu(chatId, specializationCode);

                    } else if (data.startsWith("doctor_")) {
                        String doctorIdStr = data.replace("doctor_", "");
                        try {
                            Long doctorId = Long.parseLong(doctorIdStr);
                            navigationService.goToDateMenu(chatId, doctorId);
                        } catch (NumberFormatException e) {
                            messageService.sendSimpleMessage(chatId, "❌ Ошибка: неверный ID врача");
                        }
                    } else if (data.startsWith("doctorBackSpe_")) {
                        String doctorIdStr = data.replace("doctorBackSpe_", "");
                        try {
                            Long doctorId = Long.parseLong(doctorIdStr);
                            String specialization = doctorService.getSpecializationByDoctorId(doctorId);
                            navigationService.goToDoctorMenu(chatId, specialization);
                        } catch (NumberFormatException e) {
                            messageService.sendSimpleMessage(chatId, "❌ Ошибка: неверный ID врача");
                        }
                    }else if (data.startsWith("date_")) {
                        String[] parts = data.replace("date_", "").split("_", 2);
                        if (parts.length >= 2) {
                            try {
                                Long doctorId = Long.parseLong(parts[0]);
                                LocalDate date = LocalDate.parse(parts[1],CallbackParser.DATE_FORMATTER);
                                navigationService.goToTimeMenu(chatId, doctorId, date);
                            } catch (Exception e) {
                                messageService.sendSimpleMessage(chatId, "❌ Ошибка при выборе даты"+e.getMessage());
                            }
                        }

                    } else if (data.startsWith("time_")) {
                        String[] parts = data.replace("time_", "").split("_", 2);
                        if (parts.length >= 2) {
                            try {
                                Long doctorId = Long.parseLong(parts[0]);
                                LocalDateTime time = LocalDateTime.parse(parts[1], DATE_TIME_FORMATTER);
                                navigationService.goToConfirmMenu(chatId, doctorId, time);
                            } catch (Exception e) {
                                messageService.sendSimpleMessage(chatId, "❌ Ошибка при выборе времени");
                            }
                        }

                    } else if (data.startsWith("confirm_")) {
                        String[] parts = data.replace("confirm_", "").split("_", 2);
                        if (parts.length >= 2) {
                            try {
                                Long doctorId = Long.parseLong(parts[0]);
                                LocalDateTime time = LocalDateTime.parse(parts[1], DATE_TIME_FORMATTER);
                                Appointment app = appointmentService.createAppointment(chatId, doctorId, time);
                                Doctor doctor = doctorService.getDoctorById(app.getDoctorId());
                                sendMessage(chatId, "✅ Вы успешно записаны!\n"+"Врач:"+doctor.getFirstName()+" "+doctor.getLastName()+" "+doctor.getSpecialization().getDescription());
                                navigationService.goToMainMenu(chatId);
                            } catch (Exception e) {
                                messageService.sendSimpleMessage(chatId, "❌ Ошибка подтверждения записи");
                            }
                        }

                    } else if (data.startsWith("cancel_")) {
                        String appointmentIdStr = data.replace("cancel_", "");
                        try {
                            Long appointmentId = Long.parseLong(appointmentIdStr);
                            appointmentService.cancelAppointment(appointmentId);
                            sendMessage(chatId, "✅ Запись отменена");
                            navigationService.goToMainMenu(chatId);
                        } catch (NumberFormatException e) {
                            messageService.sendSimpleMessage(chatId, "❌ Ошибка при отмене записи");
                        }

                    } else if (data.startsWith("back_from_date_to_doctor_")) {
                        String doctorIdStr = data.replace("back_from_date_to_doctor_", "");
                        try {
                            Long doctorId = Long.parseLong(doctorIdStr);
                            navigationService.goToDoctorMenu(chatId, doctorService.getSpecializationByDoctorId(doctorId));
                        } catch (NumberFormatException e) {
                            messageService.sendSimpleMessage(chatId, "❌ Ошибка при возврате к врачу");
                        }

                    } else if (data.startsWith("back_from_time_to_date_")) {
                        String[] parts = data.replace("back_from_time_to_date_", "").split("_", 2);
                        if (parts.length >= 2) {
                            try {
                                Long doctorId = Long.parseLong(parts[0]);
                                LocalDate selectedDate = LocalDate.parse(parts[1]);
                                navigationService.goToDateMenu(chatId, doctorId, selectedDate);
                                StringBuilder str = new StringBuilder();
                            } catch (Exception e) {
                                messageService.sendSimpleMessage(chatId, "❌ Ошибка возврата к дате");
                            }
                        }

                    } else {
                        messageService.sendSimpleMessage(chatId, "❌ Неизвестное действие");
                    }
                }
            }
        }
    }


    // Реализация BotMessageSender
    @Override
    public void sendMessage(Long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setReplyMarkup(keyboard);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void sendMessage(Long chatId, String text) {
        try {
            execute(SendMessage.builder()
                    .chatId(chatId.toString())
                    .text(text)
                    .build());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}