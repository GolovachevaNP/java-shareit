package ru.practicum.shareit;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.request.dto.ItemRequestAnswerDto;
import ru.practicum.shareit.user.User;

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
}