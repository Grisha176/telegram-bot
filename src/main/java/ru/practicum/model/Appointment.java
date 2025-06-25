package ru.practicum.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long chatId;
    private Long doctorId;
    private LocalDateTime appointmentTime;

    private boolean confirmed = true;
    private boolean reminded24Hours = false;
    private boolean reminded2Hours = false;
}