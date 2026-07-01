package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    // Проверка создания бронирования вещи другим пользователем
    @Test
    void createShouldCreateBooking() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        UserDto booker = createUser("Пользователь2", "email2@mail.ru");
        ItemDto item = createItem(owner.getId(), "Вещь", "Описание вещи", true);
        BookingCreateDto request = makeBooking(item.getId(), 1, 2);

        BookingDto created = bookingService.create(request, booker.getId());

        assertNotNull(created.getId());
        assertEquals(item.getId(), created.getItem().getId());
        assertEquals(booker.getId(), created.getBooker().getId());
        assertEquals(BookingStatus.WAITING, created.getStatus());
    }

    // Проверка ошибки, если владелец пытается забронировать свою вещь
    @Test
    void createShouldThrowWhenOwnerBooksOwnItem() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        ItemDto item = createItem(owner.getId(), "Вещь", "Описание вещи", true);
        BookingCreateDto request = makeBooking(item.getId(), 1, 2);

        assertThrows(NotFoundException.class, () -> bookingService.create(request, owner.getId()));
    }

    // Проверка подтверждения бронирования владельцем вещи
    @Test
    void approveShouldApproveBooking() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        UserDto booker = createUser("Пользователь2", "email2@mail.ru");
        ItemDto item = createItem(owner.getId(), "Вещь", "Описание вещи", true);
        BookingDto booking = bookingService.create(makeBooking(item.getId(), 1, 2), booker.getId());

        BookingDto approved = bookingService.approve(booking.getId(), owner.getId(), true);

        assertEquals(BookingStatus.APPROVED, approved.getStatus());
    }

    // Проверка получения бронирования пользователем, который его создал
    @Test
    void findByIdShouldReturnBookingForBooker() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        UserDto booker = createUser("Пользователь2", "email2@mail.ru");
        ItemDto item = createItem(owner.getId(), "Вещь", "Описание вещи", true);
        BookingDto booking = bookingService.create(makeBooking(item.getId(), 1, 2), booker.getId());

        BookingDto found = bookingService.findById(booking.getId(), booker.getId());

        assertEquals(booking.getId(), found.getId());
        assertEquals(item.getId(), found.getItem().getId());
    }

    // Проверка получения списка бронирований пользователя
    @Test
    void findByBookerShouldReturnBookings() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        UserDto booker = createUser("Пользователь2", "email2@mail.ru");
        ItemDto item = createItem(owner.getId(), "Вещь", "Описание вещи", true);
        bookingService.create(makeBooking(item.getId(), 1, 2), booker.getId());

        Collection<BookingDto> bookings = bookingService.findByBooker(booker.getId(), BookingState.ALL);

        assertEquals(1, bookings.size());
    }

    // Проверка получения списка бронирований вещей владельца
    @Test
    void findByOwnerShouldReturnBookings() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        UserDto booker = createUser("Пользователь2", "email2@mail.ru");
        ItemDto item = createItem(owner.getId(), "Вещь", "Описание вещи", true);
        bookingService.create(makeBooking(item.getId(), 1, 2), booker.getId());

        Collection<BookingDto> bookings = bookingService.findByOwner(owner.getId(), BookingState.ALL);

        assertEquals(1, bookings.size());
    }

    private UserDto createUser(String name, String email) {
        UserDto user = new UserDto();
        user.setName(name);
        user.setEmail(email);
        return userService.create(user);
    }

    private ItemDto createItem(Long ownerId, String name, String description, Boolean available) {
        ItemDto item = new ItemDto();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        return itemService.create(item, ownerId);
    }

    private BookingCreateDto makeBooking(Long itemId, int startDays, int endDays) {
        BookingCreateDto booking = new BookingCreateDto();
        booking.setItemId(itemId);
        booking.setStart(LocalDateTime.now().plusDays(startDays));
        booking.setEnd(LocalDateTime.now().plusDays(endDays));
        return booking;
    }
}