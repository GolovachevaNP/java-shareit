package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.dto.BookingUserDto;
import ru.practicum.shareit.item.Comment;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerModelTest {

    // Проверка модели пользователя
    @Test
    void userShouldStoreFields() {
        User user = makeUser();

        assertEquals(1L, user.getId());
        assertEquals("Пользователь", user.getName());
        assertEquals("email@mail.ru", user.getEmail());
    }

    // Проверка DTO пользователя
    @Test
    void userDtoShouldStoreFields() {
        UserDto user = new UserDto();
        user.setId(1L);
        user.setName("Пользователь");
        user.setEmail("email@mail.ru");

        assertEquals(1L, user.getId());
        assertEquals("Пользователь", user.getName());
        assertEquals("email@mail.ru", user.getEmail());
    }

    // Проверка модели вещи
    @Test
    void itemShouldStoreFields() {
        User owner = makeUser();
        ItemRequest request = makeRequest(owner);
        Item item = makeItem(owner, request);

        assertEquals(2L, item.getId());
        assertEquals("Вещь", item.getName());
        assertEquals("Описание вещи", item.getDescription());
        assertTrue(item.getAvailable());
        assertEquals(owner, item.getOwner());
        assertEquals(request, item.getRequest());
    }

    // Проверка DTO вещи
    @Test
    void itemDtoShouldStoreFields() {
        ItemDto item = new ItemDto();
        item.setId(2L);
        item.setName("Вещь");
        item.setDescription("Описание вещи");
        item.setAvailable(true);
        item.setOwnerId(1L);
        item.setRequestId(5L);

        assertEquals(2L, item.getId());
        assertEquals("Вещь", item.getName());
        assertEquals("Описание вещи", item.getDescription());
        assertTrue(item.getAvailable());
        assertEquals(1L, item.getOwnerId());
        assertEquals(5L, item.getRequestId());
    }

    // Проверка DTO вещи с комментариями
    @Test
    void itemResponseDtoShouldStoreFields() {
        CommentDto comment = new CommentDto();
        comment.setId(4L);
        comment.setText("Комментарий");
        comment.setAuthorName("Пользователь");
        comment.setCreated(LocalDateTime.of(2026, 10, 2, 12, 0));

        ItemResponseDto item = new ItemResponseDto();
        item.setId(2L);
        item.setName("Вещь");
        item.setDescription("Описание вещи");
        item.setAvailable(true);
        item.setComments(List.of(comment));

        assertEquals(2L, item.getId());
        assertEquals("Вещь", item.getName());
        assertEquals(1, item.getComments().size());
    }

    // Проверка модели комментария
    @Test
    void commentShouldStoreFields() {
        User author = makeUser();
        Item item = makeItem(author, null);
        Comment comment = new Comment();
        comment.setId(4L);
        comment.setText("Комментарий");
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.of(2026, 10, 2, 12, 0));

        assertEquals(4L, comment.getId());
        assertEquals("Комментарий", comment.getText());
        assertEquals(item, comment.getItem());
        assertEquals(author, comment.getAuthor());
        assertEquals(LocalDateTime.of(2026, 10, 2, 12, 0), comment.getCreated());
    }

    // Проверка DTO создания комментария
    @Test
    void commentCreateDtoShouldStoreFields() {
        CommentCreateDto comment = new CommentCreateDto();
        comment.setText("Комментарий");

        assertEquals("Комментарий", comment.getText());
    }

    // Проверка модели запроса вещи
    @Test
    void itemRequestShouldStoreFields() {
        User requestor = makeUser();
        ItemRequest request = makeRequest(requestor);

        assertEquals(5L, request.getId());
        assertEquals("Заказ", request.getDescription());
        assertEquals(requestor, request.getRequestor());
        assertEquals(LocalDateTime.of(2026, 10, 2, 12, 0), request.getCreated());
    }

    // Проверка DTO запроса вещи
    @Test
    void itemRequestDtoShouldStoreFields() {
        ItemRequestDto request = new ItemRequestDto();
        request.setId(5L);
        request.setDescription("Заказ");
        request.setCreated(LocalDateTime.of(2026, 10, 2, 12, 0));
        request.setItems(List.of());

        assertEquals(5L, request.getId());
        assertEquals("Заказ", request.getDescription());
        assertEquals(LocalDateTime.of(2026, 10, 2, 12, 0), request.getCreated());
        assertTrue(request.getItems().isEmpty());
    }

    // Проверка модели бронирования
    @Test
    void bookingShouldStoreFields() {
        User booker = makeUser();
        Item item = makeItem(booker, null);
        Booking booking = makeBooking(booker, item);

        assertEquals(3L, booking.getId());
        assertEquals(item, booking.getItem());
        assertEquals(booker, booking.getBooker());
        assertEquals(BookingStatus.APPROVED, booking.getStatus());
    }

    // Проверка DTO бронирования
    @Test
    void bookingDtoShouldStoreFields() {
        BookingItemDto item = new BookingItemDto(2L, "Вещь");
        BookingUserDto booker = new BookingUserDto(1L);
        BookingDto booking = new BookingDto();
        booking.setId(3L);
        booking.setStart(LocalDateTime.of(2026, 10, 2, 12, 0));
        booking.setEnd(LocalDateTime.of(2026, 10, 3, 12, 0));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);

        assertEquals(3L, booking.getId());
        assertEquals(item, booking.getItem());
        assertEquals(booker, booking.getBooker());
        assertEquals(BookingStatus.APPROVED, booking.getStatus());
    }

    // Проверка DTO создания бронирования
    @Test
    void bookingCreateDtoShouldStoreFields() {
        BookingCreateDto booking = new BookingCreateDto();
        booking.setItemId(2L);
        booking.setStart(LocalDateTime.of(2026, 10, 2, 12, 0));
        booking.setEnd(LocalDateTime.of(2026, 10, 3, 12, 0));

        assertEquals(2L, booking.getItemId());
        assertEquals(LocalDateTime.of(2026, 10, 2, 12, 0), booking.getStart());
        assertEquals(LocalDateTime.of(2026, 10, 3, 12, 0), booking.getEnd());
    }

    // Проверка состояний бронирования
    @Test
    void bookingStateShouldContainAllValues() {
        assertEquals(BookingState.ALL, BookingState.valueOf("ALL"));
        assertEquals(BookingState.CURRENT, BookingState.valueOf("CURRENT"));
        assertEquals(BookingState.PAST, BookingState.valueOf("PAST"));
        assertEquals(BookingState.FUTURE, BookingState.valueOf("FUTURE"));
        assertEquals(BookingState.WAITING, BookingState.valueOf("WAITING"));
        assertEquals(BookingState.REJECTED, BookingState.valueOf("REJECTED"));
    }

    private User makeUser() {
        User user = new User();
        user.setId(1L);
        user.setName("Пользователь");
        user.setEmail("email@mail.ru");
        return user;
    }

    private Item makeItem(User owner, ItemRequest request) {
        Item item = new Item();
        item.setId(2L);
        item.setName("Вещь");
        item.setDescription("Описание вещи");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(request);
        return item;
    }

    private ItemRequest makeRequest(User requestor) {
        ItemRequest request = new ItemRequest();
        request.setId(5L);
        request.setDescription("Заказ");
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.of(2026, 10, 2, 12, 0));
        return request;
    }

    private Booking makeBooking(User booker, Item item) {
        Booking booking = new Booking();
        booking.setId(3L);
        booking.setStart(LocalDateTime.of(2026, 10, 2, 12, 0));
        booking.setEnd(LocalDateTime.of(2026, 10, 3, 12, 0));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);
        return booking;
    }
}