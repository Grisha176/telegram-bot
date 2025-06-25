package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.Appointment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByChatIdAndConfirmedTrue(Long chatId);


    List<Appointment> findByDoctorIdAndAppointmentTimeBetweenAndConfirmed(Long doctorId, LocalDateTime start, LocalDateTime end,boolean b);

    Optional<Appointment> findByDoctorIdAndAppointmentTime(Long doctorId, LocalDateTime time);

    List<Appointment> findByDoctorIdAndAppointmentTimeAfter(Long doctorId,LocalDateTime time);

    List<Appointment> findByAppointmentTimeBetween(LocalDateTime time,LocalDateTime time2);

    @Query("SELECT a FROM Appointment a WHERE a.chatId = ?1 AND a.appointmentTime < ?2")
    List<Appointment> findByChatIdAndAppointmentTimeBefore(Long chatId, LocalDateTime now);
}