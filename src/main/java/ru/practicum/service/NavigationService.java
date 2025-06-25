package ru.practicum.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.practicum.BotMessageSender;
import ru.practicum.model.Doctor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NavigationService {

    private final DoctorService doctorService;
    private final MenuService menuService;
    private final BotMessageSender botMessageSender;

    public void goToMainMenu(Long chatId) {
        botMessageSender.sendMessage(chatId, "Выберите действие:", menuService.buildMainMenuKeyboard());
    }

    public void goToSpecializationMenu(Long chatId) {
        botMessageSender.sendMessage(chatId, "Выберите специализацию врача:", menuService.buildSpecializationMenu());
    }

    public void goToDoctorMenu(Long chatId, String specializationCode) {
        var doctors = doctorService.getDoctorsBySpecialization(specializationCode);
        botMessageSender.sendMessage(chatId, "Выберите врача:", menuService.buildDoctorMenu(doctors));
    }

    public void goToDateMenu(Long chatId, Long doctorId) {
        var dates = doctorService.getAvailableDates(doctorId);
        botMessageSender.sendMessage(chatId, "Выберите дату:", menuService.buildDateMenu(dates, doctorId));
    }

    public void goToDateMenu(Long chatId, Long doctorId, LocalDate selectedDate) {
        var times = doctorService.getAvailableSlotsForDate(doctorId, selectedDate);
        botMessageSender.sendMessage(chatId, "Выберите время:", menuService.buildTimeMenu(times, doctorId));
    }

    public void goToTimeMenu(Long chatId, Long doctorId, LocalDate selectedDate) {
        var times = doctorService.getAvailableSlotsForDate(doctorId, selectedDate);
        botMessageSender.sendMessage(chatId, "Выберите время:", menuService.buildTimeMenu(times, doctorId));
    }

    public void goToConfirmMenu(Long chatId, Long doctorId, LocalDateTime time) {
        Doctor doctor = doctorService.getDoctorById(doctorId);
        botMessageSender.sendMessage(chatId, "Подтвердите запись\n"+"Врач: "+doctor.getFirstName()+" "+doctor.getLastName()+"-"+doctor.getSpecialization().getDescription(), menuService.buildConfirmMenu(doctorId, time));
    }
}
