package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemStorage;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;
    private final ItemStorage itemStorage;
    private final UserMapper userMapper;

    private void validateUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.warn("Некорректное имя пользователя");
            throw new ConditionsNotMetException("Имя пользователя должно быть указано");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Не указан адрес электронной почты пользователя");
            throw new ConditionsNotMetException("Адрес электронной почты должен быть указан");
        }
        if (!user.getEmail().contains("@")) {
            log.warn("Некорректный адрес электронной почты пользователя");
            throw new ConditionsNotMetException("Адрес электронной почты должен содержать символ '@'");
        }
    }

    private void validateUserExists(Long userId) {
        if (userStorage.findById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }

    private void validateEmailUnique(String email) {
        if (userStorage.findByEmail(email).isPresent()) {
            throw new ConflictException("Пользователь с адресом электронной почты " + email + " уже существует");
        }
    }

    private void validateEmailUniqueForUpdate(User user) {
        Optional<User> existingUser = userStorage.findByEmail(user.getEmail());

        if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
            throw new ConflictException("Пользователь с адресом электронной почты " + user.getEmail()
                    + " уже существует");
        }
    }

    private void validateUserForUpdate(User user) {
        if (user.getName() != null && user.getName().isBlank()) {
            throw new ConditionsNotMetException("Имя пользователя должно быть указано");
        }
        if (user.getEmail() != null) {
            if (user.getEmail().isBlank()) {
                throw new ConditionsNotMetException("Адрес электронной почты должен быть указан");
            }
            if (!user.getEmail().contains("@")) {
                throw new ConditionsNotMetException("Адрес электронной почты должен содержать символ '@'");
            }
            validateEmailUniqueForUpdate(user);
        }
    }

    @Override
    public UserDto create(UserDto userDto) {
        User user = userMapper.toUser(userDto);

        validateUser(user);
        validateEmailUnique(user.getEmail());
        User createdUser = userStorage.create(user);

        log.debug("Создан пользователь: id={}", createdUser.getId());

        return userMapper.toUserDto(createdUser);
    }

    @Override
    public UserDto update(Long userId, UserDto userDto) {
        User updatedUser = userMapper.toUser(userDto);
        updatedUser.setId(userId);

        validateUserExists(updatedUser.getId());
        validateUserForUpdate(updatedUser);

        userStorage.update(updatedUser.getName(), updatedUser.getEmail(), updatedUser.getId());

        log.debug("Обновлен пользователь: id={}", updatedUser.getId());

        return userMapper.toUserDto(getUserById(updatedUser.getId()));
    }

    @Override
    public void deleteUser(Long userId) {
        validateUserExists(userId);

        itemStorage.deleteByOwnerId(userId);
        userStorage.delete(userId);
    }

    @Override
    public Collection<UserDto> findAll() {
        log.debug("Получение списка всех пользователей");

        return userStorage.findAll().stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto findById(Long userId) {
        return userMapper.toUserDto(getUserById(userId));
    }

    private User getUserById(Long userId) {
        log.debug("Получение пользователя: id={}", userId);
        return userStorage.findById(userId)
                .orElseThrow(()-> new NotFoundException("Пользователь с id = " + userId + " не найден"));
    }
}