package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ShareItServer.class)
@ActiveProfiles("test")
@Transactional
public class ItemServiceTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    // Проверка создания вещи
    @Test
    void createShouldCreateItem() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");

        ItemDto created = createItem(owner.getId(), "Вещь", "Описание вещи", true);

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
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        ItemDto item = makeItem("", "Описание вещи", true);

        assertThrows(ConditionsNotMetException.class, () -> itemService.create(item, owner.getId()));
    }

    // Проверка ошибки при создании вещи с пустым описанием
    @Test
    void createShouldThrowWhenDescriptionIsBlank() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        ItemDto item = makeItem("Вещь", " ", true);

        assertThrows(ConditionsNotMetException.class, () -> itemService.create(item, owner.getId()));
    }

    // Проверка ошибки при создании вещи без статуса доступности
    @Test
    void createShouldThrowWhenAvailableIsNull() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        ItemDto item = makeItem("Вещь", "Описание вещи", null);

        assertThrows(ConditionsNotMetException.class, () -> itemService.create(item, owner.getId()));
    }

    // Проверка обновления описания вещи
    @Test
    void updateShouldUpdateOnlyDescription() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        ItemDto created = createItem(owner.getId(), "Вещь", "Описание вещи", true);
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
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        ItemDto created = createItem(owner.getId(), "Вещь", "Описание вещи", true);
        ItemDto update = new ItemDto();
        update.setAvailable(false);

        ItemDto updated = itemService.update(created.getId(), update, owner.getId());

        assertEquals("Вещь", updated.getName());
        assertEquals("Описание вещи", updated.getDescription());
        assertEquals(false, updated.getAvailable());
    }

    // Проверка получения списка вещей владельца
    @Test
    void findByOwnerIdShouldReturnCreatedItems() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        ItemDto first = createItem(owner.getId(), "Вещь1", "Описание вещи1", true);
        ItemDto second = createItem(owner.getId(), "Вещь2", "Описание вещи2", true);

        Collection<ItemDto> items = itemService.findByOwnerId(owner.getId());

        assertTrue(items.stream().anyMatch(item -> item.getId().equals(first.getId())));
        assertTrue(items.stream().anyMatch(item -> item.getId().equals(second.getId())));
    }

    // Проверка получения вещи по идентификатору
    @Test
    void findByIdShouldReturnItem() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        ItemDto created = createItem(owner.getId(), "Вещь", "Описание вещи", true);

        ItemDto found = itemService.findById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals("Вещь", found.getName());
    }

    // Проверка ошибки при получении вещи несуществующим пользователем
    @Test
    void findByIdShouldThrowWhenRequesterNotFound() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        ItemDto created = createItem(owner.getId(), "Вещь", "Описание вещи", true);

        assertThrows(NotFoundException.class, () -> itemService.findById(created.getId(), 999L));
    }

    // Проверка поиска вещей по описанию без учета регистра
    @Test
    void searchShouldFindItemsByDescriptionIgnoringCase() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        ItemDto item = createItem(owner.getId(), "Вещь", "Описание вещи", true);

        Collection<ItemDto> result = itemService.search("ВЕЩЬ");

        assertTrue(result.stream().anyMatch(found -> found.getId().equals(item.getId())));
    }

    // Проверка пустого результата при поиске без текста
    @Test
    void searchShouldReturnEmptyListWhenTextIsBlank() {
        Collection<ItemDto> result = itemService.search(" ");

        assertTrue(result.isEmpty());
    }

    // Проверка обновления названия вещи
    @Test
    void updateShouldUpdateOnlyName() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        ItemDto created = createItem(owner.getId(), "Вещь", "Описание вещи", true);
        ItemDto update = new ItemDto();
        update.setName("Новая вещь");

        ItemDto updated = itemService.update(created.getId(), update, owner.getId());

        assertEquals("Новая вещь", updated.getName());
        assertEquals("Описание вещи", updated.getDescription());
    }

    // Проверка ошибки при обновлении вещи пустым названием
    @Test
    void updateShouldThrowWhenNameIsBlank() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        ItemDto created = createItem(owner.getId(), "Вещь", "Описание вещи", true);
        ItemDto update = new ItemDto();
        update.setName(" ");

        assertThrows(ConditionsNotMetException.class, () -> itemService.update(created.getId(), update, owner.getId()));
    }

    // Проверка ошибки при обновлении вещи пустым описанием
    @Test
    void updateShouldThrowWhenDescriptionIsBlank() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        ItemDto created = createItem(owner.getId(), "Вещь", "Описание вещи", true);
        ItemDto update = new ItemDto();
        update.setDescription(" ");

        assertThrows(ConditionsNotMetException.class, () -> itemService.update(created.getId(), update, owner.getId()));
    }

    // Проверка ошибки, если вещь обновляет не владелец
    @Test
    void updateShouldThrowWhenUserIsNotOwner() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        UserDto other = createUser("Пользователь2", "email2@mail.ru");
        ItemDto created = createItem(owner.getId(), "Вещь", "Описание вещи", true);
        ItemDto update = new ItemDto();
        update.setName("Новая вещь");

        assertThrows(NotFoundException.class, () -> itemService.update(created.getId(), update, other.getId()));
    }

    // Проверка ошибки при получении вещей несуществующего владельца
    @Test
    void findByOwnerIdShouldThrowWhenOwnerNotFound() {
        assertThrows(NotFoundException.class, () -> itemService.findByOwnerId(999L));
    }

    // Проверка ошибки, если при создании вещи указан несуществующий запрос
    @Test
    void createShouldThrowWhenRequestNotFound() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        ItemDto item = makeItem("Вещь", "Описание вещи", true);
        item.setRequestId(999L);

        assertThrows(NotFoundException.class, () -> itemService.create(item, owner.getId()));
    }

    // Проверка ошибки при добавлении комментария без текста
    @Test
    void addCommentShouldThrowWhenTextIsBlank() {
        CommentCreateDto comment = new CommentCreateDto();
        comment.setText(" ");

        assertThrows(ConditionsNotMetException.class, () -> itemService.addComment(1L, 1L, comment));
    }

    // Проверка ошибки, если пользователь не бронировал вещь перед комментарием
    @Test
    void addCommentShouldThrowWhenUserDidNotBookItem() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        UserDto author = createUser("Пользователь2", "email2@mail.ru");
        ItemDto item = createItem(owner.getId(), "Вещь", "Описание вещи", true);
        CommentCreateDto comment = new CommentCreateDto();
        comment.setText("Комментарий");

        assertThrows(ConditionsNotMetException.class,
                () -> itemService.addComment(item.getId(), author.getId(), comment));
    }

    private UserDto createUser(String name, String email) {
        UserDto user = new UserDto();
        user.setName(name);
        user.setEmail(email);
        return userService.create(user);
    }

    private ItemDto createItem(Long ownerId, String name, String description, Boolean available) {
        return itemService.create(makeItem(name, description, available), ownerId);
    }

    private ItemDto makeItem(String name, String description, Boolean available) {
        ItemDto item = new ItemDto();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        return item;
    }
}