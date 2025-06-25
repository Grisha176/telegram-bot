package ru.practicum.model;

import lombok.Getter;

@Getter
public enum Specialization {

    THERAPIST("терапевт"),
    DENTIST("дантист"),
    OPHTHALMOLOGIST("офтальмолог"),
    CARDIOLOGIST("кардиолог"),
    ENDOCRINOLOGIST("эндокринолог");

    private final String description;

    // Конструктор enum должен быть private
    private Specialization(String description) {
        this.description = description;
    }


}