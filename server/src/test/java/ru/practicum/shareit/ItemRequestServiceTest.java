package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ShareItServer.class)
@ActiveProfiles("test")
@Transactional
public class ItemRequestServiceTest {

    @Autowired
    private ItemRequestService requestService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    // Проверка создания запроса вещи
    @Test
    void createShouldCreateRequest() {
        UserDto user = createUser("Пользователь", "email@mail.ru");

        ItemRequestDto created = requestService.create(makeRequest("Запрос"), user.getId());

        assertNotNull(created.getId());
        assertEquals("Запрос", created.getDescription());
        assertNotNull(created.getCreated());
        assertNotNull(created.getItems());
        assertEquals(0, created.getItems().size());
    }

    // Проверка ошибки при создании запроса без описания
    @Test
    void createShouldThrowWhenDescriptionIsBlank() {
        UserDto user = createUser("Пользователь", "email@mail.ru");

        assertThrows(ConditionsNotMetException.class, () -> requestService.create(makeRequest(" "), user.getId()));
    }

    // Проверка ошибки при создании запроса несуществующим пользователем
    @Test
    void createShouldThrowWhenUserNotFound() {
        assertThrows(NotFoundException.class, () -> requestService.create(makeRequest("Запрос"), 999L));
    }


    // Проверка получения запросов других пользователей
    @Test
    void findAllByOtherUsersShouldExcludeOwnRequests() {
        UserDto user = createUser("Пользователь1", "email1@mail.ru");
        UserDto otherUser = createUser("Пользователь2", "email2@mail.ru");
        ItemRequestDto ownRequest = requestService.create(makeRequest("Запрос1"), user.getId());
        ItemRequestDto otherRequest = requestService.create(makeRequest("Запрос2"), otherUser.getId());

        List<ItemRequestDto> requests = List.copyOf(requestService.findAllByOtherUsers(user.getId()));

        assertTrue(requests.stream().anyMatch(request -> request.getId().equals(otherRequest.getId())));
        assertTrue(requests.stream().noneMatch(request -> request.getId().equals(ownRequest.getId())));
    }

    // Проверка получения запроса с ответами
    @Test
    void findByIdShouldReturnRequestWithAnswers() {
        UserDto requestor = createUser("Пользователь1", "emai1@mail.ru");
        UserDto owner = createUser("Пользователь2", "email2@mail.ru");
        ItemRequestDto request = requestService.create(makeRequest("Запрос"), requestor.getId());

        ItemDto answer = makeItem("Вещь", "Описание вещи", true);
        answer.setRequestId(request.getId());
        ItemDto createdItem = itemService.create(answer, owner.getId());

        ItemRequestDto found = requestService.findById(request.getId(), owner.getId());

        assertEquals(request.getId(), found.getId());
        assertEquals(1, found.getItems().size());
        assertEquals(createdItem.getId(), found.getItems().iterator().next().getId());
        assertEquals("Вещь", found.getItems().iterator().next().getName());
        assertEquals(owner.getId(), found.getItems().iterator().next().getOwnerId());
    }

    private UserDto createUser(String name, String email) {
        UserDto user = new UserDto();
        user.setName(name);
        user.setEmail(email);
        return userService.create(user);
    }

    private ItemRequestDto makeRequest(String description) {
        ItemRequestDto request = new ItemRequestDto();
        request.setDescription(description);
        return request;
    }

    private ItemDto makeItem(String name, String description, Boolean available) {
        ItemDto item = new ItemDto();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        return item;
    }
}