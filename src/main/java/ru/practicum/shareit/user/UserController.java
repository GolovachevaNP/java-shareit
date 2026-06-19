package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

@RestController
@RequestMapping(path = "/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> create(@Valid @RequestBody UserDto userDto) {
        UserDto createdUser = userService.create(userDto);
        log.info("Создан пользователь: id={}, имя={}, email={}",
                createdUser.getId(),
                createdUser.getName(),
                createdUser.getEmail());
        return ResponseEntity.ok().body(createdUser);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserDto> update(@PathVariable Long id, @RequestBody UserDto userDto) {
        UserDto updatedUser = userService.update(id, userDto);
        log.info("Обновлены данные пользователя: id={}", updatedUser.getId());

        return ResponseEntity.ok().body(updatedUser);
    }

    @GetMapping
    public ResponseEntity<Collection<UserDto>> findAll() {
        Collection<UserDto> users = userService.findAll();
        log.info("Запрошен список всех пользователей");
        return ResponseEntity.ok().body(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        UserDto user = userService.findById(id);
        log.info("Найден пользователь: id={}, имя={}", user.getId(), user.getName());
        return ResponseEntity.ok().body(user);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(@PathVariable Long userId) {
        userService.deleteUser(userId);
        log.info("Удален пользователь: id={}", userId);
        return ResponseEntity.ok().build();
    }
}