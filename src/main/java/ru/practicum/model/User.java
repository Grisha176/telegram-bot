package ru.practicum.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private Long chatId; // ID чата в Telegram

    private String name;
    private String phoneNumber;

    private LocalDateTime registeredAt; // когда пользователь начал взаимодействовать с ботом

    private boolean isBlocked = false; // можно добавить блокировку пользователя
}