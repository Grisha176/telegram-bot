package ru.practicum.service;

import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.DoctorDto;
import ru.practicum.mappers.DoctorMapper;
import ru.practicum.model.Appointment;
import ru.practicum.model.Doctor;
import ru.practicum.model.Specialization;
import ru.practicum.repository.AppointmentRepository;
import ru.practicum.repository.DoctorRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    public List<DoctorDto> getDoctorsBySpecialization(String code) {
        //System.out.println(doctorRepository.findBySpecialization(Specialization.valueOf(code.toUpperCase())).size());
        return doctorRepository.findBySpecialization(Specialization.valueOf(code.toUpperCase())).stream()
                .map(DoctorMapper::mapToDto)
                .collect(Collectors.toList());
    }

    public String getSpecializationByDoctorId(Long doctorId){
        Doctor doctor = doctorRepository.findById(doctorId).orElseThrow(() -> new NotFoundException("Доктор с id:"+doctorId+" не найден"));
        return doctor.getSpecialization().toString();
    }

    public List<LocalDateTime> getAvailableSlots(Long doctorId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekLater = now.plusWeeks(1);

        // Получаем занятые слоты
        List<Appointment> busySlots = appointmentRepository.findByDoctorIdAndAppointmentTimeAfter(doctorId, now);

        // Генерируем все возможные слоты: с 9:00 до 18:00, каждые 30 мин
        List<LocalDateTime> allSlots = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDateTime day = now.plusDays(i);

            // Пропускаем выходные
            if (day.getDayOfWeek() == DayOfWeek.SATURDAY || day.getDayOfWeek() == DayOfWeek.SUNDAY) {
                continue;
            }

            LocalTime start = LocalTime.of(9, 0);
            LocalTime end = LocalTime.of(18, 0);

            while (!start.isAfter(end)) {
                allSlots.add(LocalDateTime.of(day.toLocalDate(), start));
                start = start.plusMinutes(30);
            }
        }

        // Убираем занятые
        return allSlots.stream()
                .filter(slot -> busySlots.stream().noneMatch(busy -> busy.getAppointmentTime().isEqual(slot)))
                .toList();
    }

    public List<LocalDateTime> getAvailableSlotsForDate(Long doctorId, LocalDate date) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.atTime(23, 59);

        List<Appointment> busySlots = appointmentRepository.findByDoctorIdAndAppointmentTimeBetweenAndConfirmed(
                doctorId, dayStart, dayEnd,true);

        List<LocalDateTime> allSlots = new ArrayList<>();
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(18, 0);

        while (!start.isAfter(end)) {
            LocalDateTime slot = LocalDateTime.of(date, start);
            if (slot.isAfter(now)) {
                allSlots.add(slot);
            }
            start = start.plusMinutes(30);
        }

        return allSlots.stream()
                .filter(slot -> busySlots.stream().noneMatch(busy -> busy.getAppointmentTime().isEqual(slot)))
                .toList();
    }

    public List<LocalDate> getAvailableDates(Long doctorId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekLater = now.plusWeeks(1);

        // Получаем занятые слоты
        List<Appointment> busySlots = appointmentRepository.findByDoctorIdAndAppointmentTimeAfter(doctorId, now);

        // Генерируем все возможные дни (например, на неделю вперёд)
        List<LocalDate> allDates = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = now.plusDays(i).toLocalDate();

            // Пропускаем выходные
            if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                continue;
            }

            allDates.add(date);
        }

        // Оставляем только те дни, где есть хотя бы один свободный слот
        return allDates.stream()
                .filter(date -> hasFreeSlots(doctorId, date))
                .toList();
    }

    private boolean hasFreeSlots(Long doctorId, LocalDate date) {
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(18, 0);

        while (!start.isAfter(end)) {
            LocalDateTime slot = LocalDateTime.of(date, start);
            boolean isBusy = appointmentRepository.findByDoctorIdAndAppointmentTime(doctorId, slot).isPresent();
            if (!isBusy) return true;
            start = start.plusMinutes(30);
        }
        return false;
    }

    public Doctor getDoctorById(Long doctorId){
        return doctorRepository.findById(doctorId).orElseThrow(() -> new NotFoundException("Доктор с id:"+doctorId+" не найден"));
    }
}