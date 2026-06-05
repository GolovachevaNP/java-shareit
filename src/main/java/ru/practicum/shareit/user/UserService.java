package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

public interface UserService {
    UserDto create(UserDto userDto);

    UserDto update(Long userId, UserDto userDto);

    void deleteUser(Long userId);

    Collection<UserDto> findAll();

    UserDto findById(Long userId);
}