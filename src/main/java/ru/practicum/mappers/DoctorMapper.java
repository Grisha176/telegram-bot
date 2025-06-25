package ru.practicum.mappers;

import ru.practicum.dto.DoctorDto;
import ru.practicum.model.Doctor;

public class DoctorMapper {

    public static DoctorDto mapToDto(Doctor doctor) {
        return  DoctorDto.builder()
                .id(doctor.getId())
                .firstName(doctor.getFirstName())
                .lastName(doctor.getLastName())
                .specialization(doctor.getSpecialization().toString())
                .build();
    }
}
