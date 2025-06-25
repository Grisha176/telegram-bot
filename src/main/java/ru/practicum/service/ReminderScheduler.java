package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import ru.practicum.model.Appointment;
import ru.practicum.repository.AppointmentRepository;


import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;
    private final TelegramLongPollingBot bot;

    // Раз в минуту проверяем напоминания
    @Scheduled(fixedRate = 60000)
    public void checkReminders() {
        LocalDateTime now = LocalDateTime.now().plusHours(2);

        // Записи за 24 часа
        List<Appointment> tomorrowApps = appointmentRepository.findByAppointmentTimeBetween(now, now.plus(Duration.ofHours(24)));

        // Записи за 2 часа
        now = LocalDateTime.now();
        List<Appointment> twoHourApps = appointmentRepository.findByAppointmentTimeBetween(
                now.plus(Duration.ofHours(2)),
                now.plus(Duration.ofHours(2)).plusMinutes(10) // небольшой буфер на случай задержки
        );

        // Отправляем напоминания, но только если они ещё не были отправлены
        for (Appointment app : tomorrowApps) {
            if (!app.isReminded24Hours() && app.isConfirmed()) {
                notificationService.sendReminders(List.of(app), "🔔 У вас завтра приём!" , bot);
                mark24HoursReminded(app);
            }
        }

        for (Appointment app : twoHourApps) {
            if (!app.isReminded2Hours() && app.isConfirmed()) {
                notificationService.sendReminders(List.of(app), "⏰ У вас через 2 часа приём!", bot);
                mark2HoursReminded(app);
            }
        }
    }

    // Метод для обновления флага reminded24Hours
    private void mark24HoursReminded(Appointment appointment) {
        appointment.setReminded24Hours(true);
        appointmentRepository.save(appointment);
    }

    // Метод для обновления флага reminded2Hours
    private void mark2HoursReminded(Appointment appointment) {
        appointment.setReminded2Hours(true);
        appointmentRepository.save(appointment);
    }
}