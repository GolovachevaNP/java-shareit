package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    // Проверка создания пользователя
    @Test
    void createShouldCreateUser() {
        UserDto user = makeUser("Пользователь", "email@mail.ru");

        UserDto created = userService.create(user);

        assertNotNull(created.getId());
        assertEquals("Пользователь", created.getName());
        assertEquals("email@mail.ru", created.getEmail());
    }

    // Проверка ошибки при создании пользователя с пустым именем
    @Test
    void createShouldThrowWhenNameIsBlank() {
        UserDto user = makeUser(" ", "email@mail.ru");

        assertThrows(ConditionsNotMetException.class, () -> userService.create(user));
    }

    // Проверка ошибки при создании пользователя с пустым email
    @Test
    void createShouldThrowWhenEmailIsBlank() {
        UserDto user = makeUser("Пользователь", "");

        assertThrows(ConditionsNotMetException.class, () -> userService.create(user));
    }

    // Проверка ошибки при создании пользователя с некорректным email
    @Test
    void createShouldThrowWhenEmailIsInvalid() {
        UserDto user = makeUser("Пользователь", "email");

        assertThrows(ConditionsNotMetException.class, () -> userService.create(user));
    }

    // Проверка ошибки при создании пользователя с уже существующим email
    @Test
    void createShouldThrowWhenEmailAlreadyExists() {
        createUser("Пользователь1", "email@mail.ru");
        UserDto user = makeUser("Пользователь2", "email@mail.ru");

        assertThrows(ConflictException.class, () -> userService.create(user));
    }

    // Проверка обновления имени пользователя
    @Test
    void updateShouldUpdateOnlyName() {
        UserDto created = createUser("Пользователь1", "email@mail.ru");
        UserDto update = new UserDto();
        update.setName("Пользователь11");

        UserDto updated = userService.update(created.getId(), update);

        assertEquals(created.getId(), updated.getId());
        assertEquals("Пользователь11", updated.getName());
        assertEquals("email@mail.ru", updated.getEmail());
    }

    // Проверка обновления email пользователя
    @Test
    void updateShouldUpdateOnlyEmail() {
        UserDto created = createUser("Пользователь", "email@mail.ru");
        UserDto update = new UserDto();
        update.setEmail("new_email@mail.ru");

        UserDto updated = userService.update(created.getId(), update);

        assertEquals("Пользователь", updated.getName());
        assertEquals("new_email@mail.ru", updated.getEmail());
    }

    // Проверка получения списка всех пользователей
    @Test
    void findAllShouldReturnAllUsers() {
        createUser("Пользователь1", "email1@mail.ru");
        createUser("Пользователь2", "email2@mail.ru");

        Collection<UserDto> users = userService.findAll();

        assertEquals(2, users.size());
    }

    // Проверка удаления пользователя
    @Test
    void deleteShouldRemoveUser() {
        UserDto created = createUser("Пользователь", "email@mail.ru");

        userService.deleteUser(created.getId());

        assertThrows(NotFoundException.class, () -> userService.findById(created.getId()));
    }

    private UserDto createUser(String name, String email) {
        return userService.create(makeUser(name, email));
    }

    private UserDto makeUser(String name, String email) {
        UserDto user = new UserDto();
        user.setName(name);
        user.setEmail(email);
        return user;
    }
}