package ru.practicum.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.practicum.CallbackParser;
import ru.practicum.dto.DoctorDto;
import ru.practicum.model.Appointment;
import ru.practicum.model.Doctor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static ru.practicum.CallbackParser.DATE_TIME_FORMATTER;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final DoctorService doctorService;

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");



    public InlineKeyboardMarkup buildCancelMenu(List<Appointment> appointments) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Appointment app : appointments) {
            Doctor doctor = doctorService.getDoctorById(app.getDoctorId()); // можно получать из DoctorService
            String time = app.getAppointmentTime().format(DateTimeFormatter.ofPattern("dd.MM HH:mm"));

            String buttonText = doctor.getFirstName() + " " + doctor.getLastName() + " — " + time;
            String callbackData = "cancel_" + app.getId(); // например: cancel_456

            rows.add(List.of(createButton(buttonText, callbackData)));
        }

        // Кнопка "Назад"
        rows.add(List.of(createButton("⬅️ Назад", "main_menu")));

        return new InlineKeyboardMarkup(rows);
    }

    public InlineKeyboardMarkup buildDateMenu(List<LocalDate> dates, Long doctorId) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM");

        for (LocalDate date : dates) {
            String callbackData = "date_" + doctorId + "_" + date.format(CallbackParser.DATE_FORMATTER);
            rows.add(List.of(createButton(date.format(formatter), callbackData)));
        }

        return wrapWithBackButton(rows, "doctorBackSpe_" + doctorId);
    }
    // 🔹 Меню выбора времени
    public InlineKeyboardMarkup buildTimeMenu(List<LocalDateTime> times, Long doctorId) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        int buttonsPerRow = 3;

        for (int i = 0; i < times.size(); i += buttonsPerRow) {
            List<InlineKeyboardButton> row = new ArrayList<>();

            for (int j = 0; j < buttonsPerRow && i + j < times.size(); j++) {
                LocalDateTime time = times.get(i + j);
                String formattedTime = time.format(timeFormatter);
                String callbackData = "time_" + doctorId + "_" + time.format(DateTimeFormatter.ISO_DATE_TIME);

                row.add(createButton(formattedTime, callbackData));
            }

            rows.add(row);
        }

        return wrapWithBackButton(rows, "doctor_" + doctorId);
    }


    // другие методы: showDateMenu, showTimeMenu и т.д.

    // --- Вспомогательные методы построения меню ---
    public static InlineKeyboardMarkup buildMainMenuKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createButton("Записаться", "record"));
        row1.add(createButton("Мои записи", "my_records"));

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createButton("Отмена записи", "cancel_record"));
        row2.add(createButton("Помощь", "help"));

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(createButton("📕 Архив записей", "archive_records"));

        rows.add(row1);
        rows.add(row2);
        rows.add(row3);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }


    public static InlineKeyboardMarkup buildSpecializationMenu() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createButton("👨⚕️ Терапевт", "specialization_therapist"));
        row.add(createButton("🦷 Стоматолог", "specialization_dentist"));
        rows.add(row);

        row = new ArrayList<>();
        row.add(createButton("👁 Офтальмолог", "specialization_ophtalmologist"));
        row.add(createButton("🫀 Кардиолог", "specialization_cardiologist"));
        rows.add(row);

        return wrapWithBackButton(rows, "main_menu");
    }

    public static InlineKeyboardMarkup buildDoctorMenu(List<DoctorDto> doctors) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (DoctorDto d : doctors) {
            rows.add(List.of(
                    createButton(d.getFirstName() + " " + d.getLastName(), "doctor_" + d.getId())
            ));
        }

        return wrapWithBackButton(rows, "record");
    }

    private static InlineKeyboardMarkup wrapWithBackButton(List<List<InlineKeyboardButton>> rows, String backCallbackData) {
        rows.add(List.of(createButton("⬅️ Назад", backCallbackData)));
        return new InlineKeyboardMarkup(rows);
    }

    private static InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }
    public InlineKeyboardMarkup buildConfirmMenu(Long doctorId, LocalDateTime time) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(List.of(createButton("✅ Подтвердить", "confirm_" + doctorId + "_" + time.format(DATE_TIME_FORMATTER))));

        // Кнопка "⬅️ Назад" → к выбору времени
        String backCallbackData = "time_" + doctorId + "_" + time.format(DATE_TIME_FORMATTER);

        return wrapWithBackButton(rows, backCallbackData);
    }
}
