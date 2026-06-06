package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.InMemoryItemStorage;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;
    private UserStorage userStorage;
    private InMemoryItemStorage itemStorage;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
        itemStorage = new InMemoryItemStorage();
        userService = new UserServiceImpl(userStorage, itemStorage, Mappers.getMapper(UserMapper.class));
    }

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

    // Проверка удаления вещей пользователя при удалении пользователя
    @Test
    void deleteShouldRemoveUserItems() {
        UserDto created = createUser("Пользователь", "email@mail.ru");
        User owner = userStorage.findById(created.getId()).get();
        Item item = new Item();
        item.setName("Вещь");
        item.setDescription("Описание вещи");
        item.setAvailable(true);
        item.setOwner(owner);
        itemStorage.create(item);

        userService.deleteUser(created.getId());

        assertEquals(0, itemStorage.search("Вещь").size());
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