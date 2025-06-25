package ru.practicum.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.practicum.BotMessageSender;
import ru.practicum.model.Appointment;
import ru.practicum.repository.AppointmentRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorService doctorService;
    private final BotMessageSender botMessageSender;
    private final MenuService menuService;


    public void sendArchiveRecords(Long chatId) {
        List<Appointment> archive = appointmentRepository.findByChatIdAndAppointmentTimeBefore(chatId, LocalDateTime.now());

        if (archive.isEmpty()) {
            botMessageSender.sendMessage(chatId, "📂 Архив пуст", menuService.buildMainMenuKeyboard());
            return;
        }

        StringBuilder sb = new StringBuilder("📂 Архив записей:\n\n");

        for (Appointment a : archive) {
            sb.append("👨‍⚕️ ").append(doctorService.getDoctorById(a.getDoctorId()).getFirstName()).append("\n");
            sb.append("📅 ").append(a.getAppointmentTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))).append("\n\n");
        }

        botMessageSender.sendMessage(chatId, sb.toString(), menuService.buildMainMenuKeyboard());
    }

    // 🔹 Отправить "Мои записи"
    public void sendMyRecords(Long chatId) {
        List<Appointment> records = appointmentRepository.findByChatIdAndConfirmedTrue(chatId);

        if (records.isEmpty()) {
            botMessageSender.sendMessage(chatId,"📅 У вас пока нет записей", menuService.buildMainMenuKeyboard());
            return;
        }

        StringBuilder sb = new StringBuilder("📌 Ваши записи:\n\n");

        for (Appointment a : records) {
            sb.append("👨‍⚕️ ").append(doctorService.getDoctorById(a.getDoctorId()).getFirstName()).append(" ")
                    .append(doctorService.getDoctorById(a.getDoctorId()).getLastName()).append("\n");
            sb.append("📅 ").append(a.getAppointmentTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))).append("\n\n");
        }

        botMessageSender.sendMessage(chatId, sb.toString(), menuService.buildMainMenuKeyboard());
    }

    // 🔹 Отправить меню отмены
    public void sendCancelMenu(Long chatId) {
        List<Appointment> records = appointmentRepository.findByChatIdAndConfirmedTrue(chatId);
        if(records.isEmpty()){
            botMessageSender.sendMessage(chatId,"📅 У вас пока нет записей", menuService.buildMainMenuKeyboard());
            return;
        }
        InlineKeyboardMarkup keyboard = menuService.buildCancelMenu(records);
        botMessageSender.sendMessage(chatId, "Выберите запись для отмены:", keyboard);
    }

    // ✅ Метод createAppointment — создаёт новую запись
    public Appointment createAppointment(Long chatId, Long doctorId, LocalDateTime appointmentTime) {
        Appointment appointment = new Appointment();
        appointment.setChatId(chatId);
        appointment.setDoctorId(doctorId);
        appointment.setAppointmentTime(appointmentTime);
        appointment.setConfirmed(true);
        appointment.setReminded24Hours(false);
        appointment.setReminded2Hours(false);

        appointment =  appointmentRepository.save(appointment);
        return appointment;
    }

    // 🚫 Отменяет запись по ID
    public void cancelAppointment(Long appointmentId) {
        Appointment app = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("❌ Запись не найдена"));

        app.setConfirmed(false);
        appointmentRepository.save(app);
    }

    // 📅 Получает записи пользователя
    public List<Appointment> getUserAppointments(Long chatId) {
        return appointmentRepository.findByChatIdAndConfirmedTrue(chatId);
    }

    // ⏳ Проверяет, свободно ли время
    public boolean isSlotFree(Long doctorId, LocalDateTime time) {
        return !isSlotTaken(doctorId, time);
    }

    private boolean isSlotTaken(Long doctorId, LocalDateTime time) {
        return appointmentRepository.findByDoctorIdAndAppointmentTime(doctorId, time).isPresent();
    }
}