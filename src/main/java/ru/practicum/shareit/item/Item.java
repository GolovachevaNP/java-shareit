package ru.practicum.shareit.item;

import lombok.Data;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

@Data
public class Item {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private User owner;
    private ItemRequest request; // если вещь была создана по запросу другого пользователя, то в этом поле будет
                                 // храниться ссылка на соответствующий запрос
}