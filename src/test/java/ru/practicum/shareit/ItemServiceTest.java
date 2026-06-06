package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.InMemoryItemStorage;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.ItemServiceImpl;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.user.InMemoryUserStorage;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserStorage;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class ItemServiceTest {

    private ItemService itemService;
    private UserStorage userStorage;
    private User owner;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
        itemService = new ItemServiceImpl(new InMemoryItemStorage(), userStorage, Mappers.getMapper(ItemMapper.class));

        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@mail.ru");
        owner = userStorage.create(owner);
    }

    // Проверка создания вещи
    @Test
    void createShouldCreateItem() {
        ItemDto created = createItem("Вещь", "Описание вещи", true);

        assertNotNull(created.getId());
        assertEquals("Вещь", created.getName());
        assertEquals("Описание вещи", created.getDescription());
        assertTrue(created.getAvailable());
        assertEquals(owner.getId(), created.getOwnerId());
    }

    // Проверка ошибки при создании вещи несуществующим пользователем
    @Test
    void createShouldThrowWhenUserNotFound() {
        ItemDto item = makeItem("Вещь", "Описание вещи", true);

        assertThrows(NotFoundException.class, () -> itemService.create(item, 999L));
    }

    // Проверка ошибки при создании вещи с пустым названием
    @Test
    void createShouldThrowWhenNameIsBlank() {
        ItemDto item = makeItem("", "Описание вещи", true);

        assertThrows(ConditionsNotMetException.class, () -> itemService.create(item, owner.getId()));
    }

    // Проверка ошибки при создании вещи с пустым описанием
    @Test
    void createShouldThrowWhenDescriptionIsBlank() {
        ItemDto item = makeItem("Вещь", " ", true);

        assertThrows(ConditionsNotMetException.class, () -> itemService.create(item, owner.getId()));
    }

    // Проверка ошибки при создании вещи без статуса доступности
    @Test
    void createShouldThrowWhenAvailableIsNull() {
        ItemDto item = makeItem("Вещь", "Описание вещи", null);

        assertThrows(ConditionsNotMetException.class, () -> itemService.create(item, owner.getId()));
    }

    // Проверка обновления описания вещи
    @Test
    void updateShouldUpdateOnlyDescription() {
        ItemDto created = createItem("Вещь", "Описание вещи", true);
        ItemDto update = new ItemDto();
        update.setDescription("Новое описание вещи");

        ItemDto updated = itemService.update(created.getId(), update, owner.getId());

        assertEquals("Вещь", updated.getName());
        assertEquals("Новое описание вещи", updated.getDescription());
        assertTrue(updated.getAvailable());
    }

    // Проверка обновления статуса доступности вещи
    @Test
    void updateShouldUpdateOnlyAvailable() {
        ItemDto created = createItem("Вещь", "Описание вещи", true);
        ItemDto update = new ItemDto();
        update.setAvailable(false);

        ItemDto updated = itemService.update(created.getId(), update, owner.getId());

        assertEquals("Вещь", updated.getName());
        assertEquals("Описание вещи", updated.getDescription());
        assertEquals(false, updated.getAvailable());
    }


    // Проверка получения списка вещей конкретного владельца
    @Test
    void findByOwnerIdShouldReturnOnlyOwnerItems() {
        createItem("Вещь1", "Описание вещи1", true);
        createItem("Вещь2", "Описание вещи2", true);

        Collection<ItemDto> items = itemService.findByOwnerId(owner.getId());

        assertEquals(2, items.size());
    }

    // Проверка получения вещи по идентификатору
    @Test
    void findByIdShouldReturnItem() {
        ItemDto created = createItem("Вещь", "Описание вещи", true);

        ItemDto found = itemService.findById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals("Вещь", found.getName());
    }

    // Проверка поиска вещей по описанию без учета регистра
    @Test
    void searchShouldFindItemsByDescriptionIgnoringCase() {
        createItem("Вещь", "Описание вещи", true);

        Collection<ItemDto> result = itemService.search("ВЕЩЬ");

        assertEquals(1, result.size());
    }

    private ItemDto createItem(String name, String description, Boolean available) {
        return createItem(makeItem(name, description, available));
    }

    private ItemDto createItem(ItemDto item) {
        return itemService.create(item, owner.getId());
    }

    private ItemDto makeItem(String name, String description, Boolean available) {
        ItemDto item = new ItemDto();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        return item;
    }
}