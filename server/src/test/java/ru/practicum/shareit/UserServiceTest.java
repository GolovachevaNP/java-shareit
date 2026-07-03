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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ShareItServer.class)
@ActiveProfiles("test")
@Transactional
public class UserServiceTest {

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
        UserDto first = createUser("Пользователь1", "email1@mail.ru");
        UserDto second = createUser("Пользователь2", "email2@mail.ru");

        Collection<UserDto> users = userService.findAll();

        assertTrue(users.stream().anyMatch(user -> user.getId().equals(first.getId())));
        assertTrue(users.stream().anyMatch(user -> user.getId().equals(second.getId())));
    }

    // Проверка удаления пользователя
    @Test
    void deleteShouldRemoveUser() {
        UserDto created = createUser("Пользователь", "email@mail.ru");

        userService.deleteUser(created.getId());

        assertThrows(NotFoundException.class, () -> userService.findById(created.getId()));
    }

    // Проверка ошибки при обновлении пользователя пустым именем
    @Test
    void updateShouldThrowWhenNameIsBlank() {
        UserDto created = createUser("Пользователь", "email@mail.ru");
        UserDto update = new UserDto();
        update.setName(" ");

        assertThrows(ConditionsNotMetException.class, () -> userService.update(created.getId(), update));
    }

    // Проверка ошибки при обновлении пользователя пустым email
    @Test
    void updateShouldThrowWhenEmailIsBlank() {
        UserDto created = createUser("Пользователь", "email@mail.ru");
        UserDto update = new UserDto();
        update.setEmail(" ");

        assertThrows(ConditionsNotMetException.class, () -> userService.update(created.getId(), update));
    }

    // Проверка ошибки при обновлении пользователя некорректным email
    @Test
    void updateShouldThrowWhenEmailIsInvalid() {
        UserDto created = createUser("Пользователь", "email@mail.ru");
        UserDto update = new UserDto();
        update.setEmail("email");

        assertThrows(ConditionsNotMetException.class, () -> userService.update(created.getId(), update));
    }

    // Проверка ошибки при обновлении пользователя уже занятым email
    @Test
    void updateShouldThrowWhenEmailAlreadyExists() {
        UserDto first = createUser("Пользователь", "email@mail.ru");
        createUser("Пользователь2", "email2@mail.ru");
        UserDto update = new UserDto();
        update.setEmail("email2@mail.ru");

        assertThrows(ConflictException.class, () -> userService.update(first.getId(), update));
    }

    // Проверка получения пользователя по id
    @Test
    void findByIdShouldReturnUser() {
        UserDto created = createUser("Пользователь", "email@mail.ru");

        UserDto found = userService.findById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals("Пользователь", found.getName());
    }

    // Проверка ошибки при обновлении несуществующего пользователя
    @Test
    void updateShouldThrowWhenUserNotFound() {
        UserDto update = new UserDto();
        update.setName("Пользователь");

        assertThrows(NotFoundException.class, () -> userService.update(999L, update));
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