package ru.practicum.shareit.request;

import lombok.Data;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@Data
public class ItemRequest {
    private Long id;
    private String description;
    private User requestor; //пользователь, создавший запрос
    private LocalDateTime created; // дата и время создания запроса
}