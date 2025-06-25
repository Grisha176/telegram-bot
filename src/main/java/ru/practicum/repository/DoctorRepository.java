package ru.practicum.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Doctor;
import ru.practicum.model.Specialization;

import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findBySpecialization(Specialization specializationCode);
}