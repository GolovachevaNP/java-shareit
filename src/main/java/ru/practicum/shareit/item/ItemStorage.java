package ru.practicum.shareit.item;

import java.util.Collection;
import java.util.Optional;

public interface ItemStorage {

    // создание вещи
    Item create(Item item);

    // обновление информации о вещи по id
    void update(String itemName, String itemDescription, Boolean itemAvailable, Long itemId);

    // получение списка всех вещей
    Collection<Item> findAll();

    // получение всех вещей конкретного владельца
    Collection<Item> findByOwnerId(Long ownerId);

    // поиск доступных вещей по тексту в названии или описании
    Collection<Item> search(String text);

    // поиск вещи по id
    Optional<Item> findById(Long id);

    // удаление всех вещей конкретного владельца
    void deleteByOwnerId(Long ownerId);
}