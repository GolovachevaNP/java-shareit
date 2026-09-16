package ru.practicum.shareit;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.Comment;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestAnswerDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ServerSupportTest {

    // Проверка создания главного класса приложения
    @Test
    void shareItServerShouldBeCreated() {
        assertDoesNotThrow(ShareItServer::new);
    }

    // Проверка ответа с ошибкой
    @Test
    void errorResponseShouldStoreMessage() {
        ErrorResponse response = new ErrorResponse("Ошибка");

        assertEquals("Ошибка", response.getError());
    }

    // Проверка обработки ошибки валидации
    @Test
    void errorHandlerShouldHandleValidationException() {
        ErrorHandler handler = new ErrorHandler();

        ErrorResponse response = handler.handleValidationException(new ValidationException("Ошибка"));

        assertTrue(response.getError().contains("Ошибка"));
    }

    // Проверка обработки ошибки поиска
    @Test
    void errorHandlerShouldHandleNotFoundException() {
        ErrorHandler handler = new ErrorHandler();

        ErrorResponse response = handler.handleNotFoundException(new NotFoundException("Ошибка"));

        assertEquals("Ошибка", response.getError());
    }

    // Проверка обработки конфликта данных
    @Test
    void errorHandlerShouldHandleConflictException() {
        ErrorHandler handler = new ErrorHandler();

        ErrorResponse response = handler.handleConflictException(new ConflictException("Ошибка"));

        assertEquals("Ошибка", response.getError());
    }

    // Проверка обработки ошибки доступа
    @Test
    void errorHandlerShouldHandleForbiddenException() {
        ErrorHandler handler = new ErrorHandler();

        ErrorResponse response = handler.handleForbiddenException(new ForbiddenException("Ошибка"));

        assertEquals("Ошибка", response.getError());
    }

    // Проверка обработки непредвиденной ошибки
    @Test
    void errorHandlerShouldHandleThrowable() {
        ErrorHandler handler = new ErrorHandler();

        ErrorResponse response = handler.handleThrowable(new RuntimeException("Ошибка"));

        assertNotNull(response.getError());
    }

    // Проверка DTO бронирования вещи
    @Test
    void itemBookingDtoShouldStoreFields() {
        ItemBookingDto booking = new ItemBookingDto(1L, 2L);

        assertEquals(1L, booking.getId());
        assertEquals(2L, booking.getBookerId());
    }

    // Проверка DTO ответа на запрос вещи
    @Test
    void itemRequestAnswerDtoShouldStoreFields() {
        ItemRequestAnswerDto answer = new ItemRequestAnswerDto(1L, "Вещь", 2L);

        assertEquals(1L, answer.getId());
        assertEquals("Вещь", answer.getName());
        assertEquals(2L, answer.getOwnerId());
    }

    // Проверка маппера бронирования
    @Test
    void bookingMapperShouldCreateBookingDto() {
        Booking booking = makeBooking();
        BookingMapper mapper = new BookingMapper();

        BookingDto dto = mapper.toBookingDto(booking);

        assertEquals(3L, dto.getId());
        assertEquals(BookingStatus.APPROVED, dto.getStatus());
        assertEquals(2L, dto.getItem().getId());
        assertEquals("Вещь", dto.getItem().getName());
        assertEquals(1L, dto.getBooker().getId());
    }

    // Проверка преобразования пользователя в DTO
    @Test
    void userMapperShouldCreateUserDto() {
        UserMapper mapper = Mappers.getMapper(UserMapper.class);
        User user = makeUser();

        UserDto dto = mapper.toUserDto(user);

        assertEquals(1L, dto.getId());
        assertEquals("Пользователь", dto.getName());
        assertEquals("email@mail.ru", dto.getEmail());
    }

    // Проверка преобразования DTO в пользователя
    @Test
    void userMapperShouldCreateUser() {
        UserMapper mapper = Mappers.getMapper(UserMapper.class);
        UserDto dto = new UserDto();
        dto.setId(1L);
        dto.setName("Пользователь");
        dto.setEmail("email@mail.ru");

        User user = mapper.toUser(dto);

        assertEquals(1L, user.getId());
        assertEquals("Пользователь", user.getName());
        assertEquals("email@mail.ru", user.getEmail());
    }

    // Проверка преобразования вещи в DTO
    @Test
    void itemMapperShouldCreateItemDto() {
        ItemMapper mapper = Mappers.getMapper(ItemMapper.class);
        Item item = makeItem();

        ItemDto dto = mapper.toItemDto(item);

        assertEquals(2L, dto.getId());
        assertEquals("Вещь", dto.getName());
        assertEquals("Описание вещи", dto.getDescription());
        assertEquals(4L, dto.getOwnerId());
        assertEquals(5L, dto.getRequestId());
    }

    // Проверка преобразования вещи в DTO для ответа
    @Test
    void itemMapperShouldCreateItemResponseDto() {
        ItemMapper mapper = Mappers.getMapper(ItemMapper.class);
        Item item = makeItem();

        ItemResponseDto dto = mapper.toItemResponseDto(item);

        assertEquals(2L, dto.getId());
        assertEquals("Вещь", dto.getName());
        assertEquals("Описание вещи", dto.getDescription());
        assertEquals(4L, dto.getOwnerId());
        assertEquals(5L, dto.getRequestId());
    }

    // Проверка преобразования DTO в вещь
    @Test
    void itemMapperShouldCreateItem() {
        ItemMapper mapper = Mappers.getMapper(ItemMapper.class);
        ItemDto dto = new ItemDto();
        dto.setId(2L);
        dto.setName("Вещь");
        dto.setDescription("Описание вещи");
        dto.setAvailable(true);

        Item item = mapper.toItem(dto);

        assertEquals(2L, item.getId());
        assertEquals("Вещь", item.getName());
        assertEquals("Описание вещи", item.getDescription());
        assertEquals(true, item.getAvailable());
    }

    // Проверка преобразования комментария в DTO
    @Test
    void commentMapperShouldCreateCommentDto() {
        CommentMapper mapper = Mappers.getMapper(CommentMapper.class);
        Comment comment = new Comment();
        comment.setId(4L);
        comment.setText("Комментарий");
        comment.setAuthor(makeUser());
        comment.setCreated(LocalDateTime.of(2026, 10, 2, 12, 0));

        CommentDto dto = mapper.toCommentDto(comment);

        assertEquals(4L, dto.getId());
        assertEquals("Комментарий", dto.getText());
        assertEquals("Пользователь", dto.getAuthorName());
    }

    private Booking makeBooking() {
        User booker = new User();
        booker.setId(1L);
        booker.setName("Пользователь");
        booker.setEmail("email@mail.ru");

        User owner = new User();
        owner.setId(4L);
        owner.setName("Пользователь");
        owner.setEmail("email@mail.ru");

        Item item = new Item();
        item.setId(2L);
        item.setName("Вещь");
        item.setDescription("Описание вещи");
        item.setAvailable(true);
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(3L);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setStatus(BookingStatus.APPROVED);
        booking.setBooker(booker);
        booking.setItem(item);
        return booking;
    }

    private User makeUser() {
        User user = new User();
        user.setId(1L);
        user.setName("Пользователь");
        user.setEmail("email@mail.ru");
        return user;
    }

    private Item makeItem() {
        User owner = new User();
        owner.setId(4L);
        owner.setName("Пользователь");
        owner.setEmail("email@mail.ru");

        ItemRequest request = new ItemRequest();
        request.setId(5L);
        request.setDescription("Заказ");
        request.setRequestor(makeUser());
        request.setCreated(LocalDateTime.of(2026, 10, 2, 12, 0));

        Item item = new Item();
        item.setId(2L);
        item.setName("Вещь");
        item.setDescription("Описание вещи");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(request);
        return item;
    }
}