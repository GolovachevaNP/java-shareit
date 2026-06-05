package ru.practicum.shareit.user;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {

    // создание пользователя
    User create(User user);

    // обновление пользователя
    void update(String userName, String userEmail, Long userId);

    // получение списка всех пользователей
    Collection<User> findAll();

    // получение конкретного пользователя
    Optional<User> findById(Long id);

    // удаление пользователя
    void delete(Long id);

    // проверка уникальности email
    Optional<User> findByEmail(String email);
}