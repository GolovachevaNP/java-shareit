package ru.practicum.shareit.user;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // имя или логин пользователя

    @Column(nullable = false, unique = true, length = 512)
    private String email; // два пользователя не могут иметь одинаковый адрес электронной почты
}