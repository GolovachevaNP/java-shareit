package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;
    private final ItemMapper itemMapper;

    private void validateItem(Item item) {
        if (item.getName() == null || item.getName().isBlank()) {
            log.warn("Некорректное название вещи");
            throw new ConditionsNotMetException("Название вещи не должно быть пустым");
        }
        if (item.getDescription() == null || item.getDescription().isBlank()) {
            log.warn("Некорректное описание вещи");
            throw new ConditionsNotMetException("Описание вещи не должно быть пустым");
        }
        if (item.getAvailable() == null) {
            log.warn("Не указан статус доступности вещи");
            throw new ConditionsNotMetException("Необходимо указать статус доступности вещи");
        }
    }

    private void validateItemForUpdate(Item item) {
        if (item.getName() != null && item.getName().isBlank()) {
            log.warn("Некорректное название вещи");
            throw new ConditionsNotMetException("Название вещи не должно быть пустым");
        }
        if (item.getDescription() != null && item.getDescription().isBlank()) {
            log.warn("Некорректное описание вещи");
            throw new ConditionsNotMetException("Описание вещи не должно быть пустым");
        }
    }

    private void validateUserExists(Long userId) {
        if (userStorage.findById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }

    private void validateItemExists(Long itemId) {
        if (itemStorage.findById(itemId).isEmpty()) {
            throw new NotFoundException("Вещь с id = " + itemId + " не найдена");
        }
    }

    private void validateOwner(Item item, Long userId) {
        if (item.getOwner() == null || !item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не владелец вещи");
        }
    }

    private Item getItemById(Long itemId) {
        log.debug("Получение вещи: id={}", itemId);
        return itemStorage.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id = " + itemId + " не найдена"));
    }

    @Override
    public ItemDto create(ItemDto itemDto, Long userId) {
        Item item = itemMapper.toItem(itemDto);

        validateItem(item);

        User owner = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        item.setOwner(owner);

        Item createdItem = itemStorage.create(item);

        log.debug("Создана вещь: id={}, владелец id={}", createdItem.getId(), userId);

        return itemMapper.toItemDto(createdItem);
    }

    @Override
    public ItemDto update(Long itemId, ItemDto itemDto, Long userId) {
        Item updatedItem = itemMapper.toItem(itemDto);

        validateUserExists(userId);
        validateItemExists(itemId);

        Item savedItem = getItemById(itemId);
        validateOwner(savedItem, userId);
        validateItemForUpdate(updatedItem);

        itemStorage.update(updatedItem.getName(), updatedItem.getDescription(), updatedItem.getAvailable(), itemId);
        log.debug("Обновлена информация о вещи: id={}", itemId);

        return itemMapper.toItemDto(getItemById(itemId));
    }

    @Override
    public Collection<ItemDto> findByOwnerId(Long ownerId) {
        validateUserExists(ownerId);

        log.debug("Получение списка вещей владельца id={}", ownerId);

        return itemStorage.findByOwnerId(ownerId).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto findById(Long itemId) {
        return itemMapper.toItemDto(getItemById(itemId));
    }

    @Override
    public Collection<ItemDto> search(String text) {
        log.debug("Поиск вещей");

        return itemStorage.search(text).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}