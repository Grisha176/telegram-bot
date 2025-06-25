package ru.practicum.dto;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String specialization;
}
