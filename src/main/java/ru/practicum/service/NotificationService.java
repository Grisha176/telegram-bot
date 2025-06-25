package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.practicum.model.Appointment;
import ru.practicum.model.Doctor;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final DoctorService doctorService;


    public void sendReminders(List<Appointment> apps, String messageText, AbsSender bot) {
        for (Appointment app : apps) {
            Doctor doctor = doctorService.getDoctorById(app.getDoctorId());
            try {
                bot.execute(SendMessage.builder()
                        .chatId(app.getChatId().toString())
                        .text(messageText + "\n\n" +
                                "Врач: " + doctor.getFirstName() + " "+ doctor.getLastName() + "\n" +
                                "Дата: " + app.getAppointmentTime().format(DateTimeFormatter.ofPattern("dd.MM HH:mm")))
                        .build());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}