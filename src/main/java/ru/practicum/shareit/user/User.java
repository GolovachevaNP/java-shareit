package ru.practicum.shareit.user;

import lombok.Data;

@Data
public class User {
    private Long id;
    private String name; // имя или логин пользователя
    private String email; // два пользователя не могут иметь одинаковый адрес электронной почты
}