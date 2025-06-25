package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.model.User;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getOrCreateUser(Long chatId) {
        return userRepository.findById(chatId).orElseGet(() -> createUser(chatId));
    }

    private User createUser(Long chatId) {
        User user = new User();
        user.setChatId(chatId);
        user.setName("Не указано");
        user.setPhoneNumber("Не указано");
        user.setRegisteredAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public void updateUserName(Long chatId, String name) {
        User user = getOrCreateUser(chatId);
        user.setName(name);
        userRepository.save(user);
    }
}