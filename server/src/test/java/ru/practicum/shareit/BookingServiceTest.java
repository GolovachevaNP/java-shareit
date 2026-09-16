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
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.ForbiddenException;
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

    // Проверка ошибки, если вещь недоступна для бронирования
    @Test
    void createShouldThrowWhenItemIsNotAvailable() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        UserDto booker = createUser("Пользователь2", "email2@mail.ru");
        ItemDto item = createItem(owner.getId(), "Вещь", "Описание вещи", false);
        BookingCreateDto request = makeBooking(item.getId(), 1, 2);

        assertThrows(ConditionsNotMetException.class, () -> bookingService.create(request, booker.getId()));
    }

    // Проверка ошибки, если не указана дата начала бронирования
    @Test
    void createShouldThrowWhenStartIsNull() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        UserDto booker = createUser("Пользователь2", "email2@mail.ru");
        ItemDto item = createItem(owner.getId(), "Вещь", "Описание вещи", true);
        BookingCreateDto request = makeBooking(item.getId(), 1, 2);
        request.setStart(null);

        assertThrows(ConditionsNotMetException.class, () -> bookingService.create(request, booker.getId()));
    }

    // Проверка ошибки, если дата начала бронирования уже прошла
    @Test
    void createShouldThrowWhenStartIsInPast() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        UserDto booker = createUser("Пользователь2", "email2@mail.ru");
        ItemDto item = createItem(owner.getId(), "Вещь", "Описание вещи", true);
        BookingCreateDto request = makeBooking(item.getId(), 1, 2);
        request.setStart(LocalDateTime.now().minusDays(1));

        assertThrows(ConditionsNotMetException.class, () -> bookingService.create(request, booker.getId()));
    }

    // Проверка ошибки, если дата окончания раньше даты начала
    @Test
    void createShouldThrowWhenEndIsBeforeStart() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        UserDto booker = createUser("Пользователь2", "email2@mail.ru");
        ItemDto item = createItem(owner.getId(), "Вещь", "Описание вещи", true);
        BookingCreateDto request = makeBooking(item.getId(), 2, 1);

        assertThrows(ConditionsNotMetException.class, () -> bookingService.create(request, booker.getId()));
    }

    // Проверка отклонения бронирования владельцем вещи
    @Test
    void approveShouldRejectBooking() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        UserDto booker = createUser("Пользователь2", "email2@mail.ru");
        ItemDto item = createItem(owner.getId(), "Вещь", "Описание вещи", true);
        BookingDto booking = bookingService.create(makeBooking(item.getId(), 1, 2), booker.getId());

        BookingDto rejected = bookingService.approve(booking.getId(), owner.getId(), false);

        assertEquals(BookingStatus.REJECTED, rejected.getStatus());
    }

    // Проверка ошибки, если бронирование подтверждают второй раз
    @Test
    void approveShouldThrowWhenBookingAlreadyChecked() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        UserDto booker = createUser("Пользователь2", "email2@mail.ru");
        ItemDto item = createItem(owner.getId(), "Вещь", "Описание вещи", true);
        BookingDto booking = bookingService.create(makeBooking(item.getId(), 1, 2), booker.getId());
        bookingService.approve(booking.getId(), owner.getId(), true);

        assertThrows(ConditionsNotMetException.class,
                () -> bookingService.approve(booking.getId(), owner.getId(), true));
    }

    // Проверка ошибки, если решение по бронированию не указано
    @Test
    void approveShouldThrowWhenApprovedIsNull() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        UserDto booker = createUser("Пользователь2", "email2@mail.ru");
        ItemDto item = createItem(owner.getId(), "Вещь", "Описание вещи", true);
        BookingDto booking = bookingService.create(makeBooking(item.getId(), 1, 2), booker.getId());

        assertThrows(ConditionsNotMetException.class,
                () -> bookingService.approve(booking.getId(), owner.getId(), null));
    }

    // Проверка ошибки, если бронирование подтверждает не владелец вещи
    @Test
    void approveShouldThrowWhenUserIsNotOwner() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        UserDto booker = createUser("Пользователь2", "email2@mail.ru");
        UserDto other = createUser("Пользователь3", "email3@mail.ru");
        ItemDto item = createItem(owner.getId(), "Вещь", "Описание вещи", true);
        BookingDto booking = bookingService.create(makeBooking(item.getId(), 1, 2), booker.getId());

        assertThrows(ForbiddenException.class,
                () -> bookingService.approve(booking.getId(), other.getId(), true));
    }

    // Проверка получения бронирования владельцем вещи
    @Test
    void findByIdShouldReturnBookingForOwner() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        UserDto booker = createUser("Пользователь2", "email2@mail.ru");
        ItemDto item = createItem(owner.getId(), "Вещь", "Описание вещи", true);
        BookingDto booking = bookingService.create(makeBooking(item.getId(), 1, 2), booker.getId());

        BookingDto found = bookingService.findById(booking.getId(), owner.getId());

        assertEquals(booking.getId(), found.getId());
    }

    // Проверка ошибки, если бронирование смотрит посторонний пользователь
    @Test
    void findByIdShouldThrowWhenUserIsNotBookerOrOwner() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        UserDto booker = createUser("Пользователь2", "email2@mail.ru");
        UserDto other = createUser("Пользователь3", "email3@mail.ru");
        ItemDto item = createItem(owner.getId(), "Вещь", "Описание вещи", true);
        BookingDto booking = bookingService.create(makeBooking(item.getId(), 1, 2), booker.getId());

        assertThrows(NotFoundException.class, () -> bookingService.findById(booking.getId(), other.getId()));
    }

    // Проверка получения будущих бронирований пользователя
    @Test
    void findByBookerShouldReturnFutureBookings() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        UserDto booker = createUser("Пользователь2", "email2@mail.ru");
        ItemDto item = createItem(owner.getId(), "Вещь", "Описание вещи", true);
        bookingService.create(makeBooking(item.getId(), 1, 2), booker.getId());

        Collection<BookingDto> bookings = bookingService.findByBooker(booker.getId(), BookingState.FUTURE);

        assertEquals(1, bookings.size());
    }

    // Проверка пустого списка текущих бронирований пользователя
    @Test
    void findByBookerShouldReturnEmptyCurrentBookings() {
        UserDto booker = createUser("Пользователь", "email@mail.ru");

        Collection<BookingDto> bookings = bookingService.findByBooker(booker.getId(), BookingState.CURRENT);

        assertTrue(bookings.isEmpty());
    }

    // Проверка пустого списка завершенных бронирований пользователя
    @Test
    void findByBookerShouldReturnEmptyPastBookings() {
        UserDto booker = createUser("Пользователь", "email@mail.ru");

        Collection<BookingDto> bookings = bookingService.findByBooker(booker.getId(), BookingState.PAST);

        assertTrue(bookings.isEmpty());
    }

    // Проверка получения будущих бронирований владельца
    @Test
    void findByOwnerShouldReturnFutureBookings() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        UserDto booker = createUser("Пользователь2", "email2@mail.ru");
        ItemDto item = createItem(owner.getId(), "Вещь", "Описание вещи", true);
        bookingService.create(makeBooking(item.getId(), 1, 2), booker.getId());

        Collection<BookingDto> bookings = bookingService.findByOwner(owner.getId(), BookingState.FUTURE);

        assertEquals(1, bookings.size());
    }

    // Проверка пустого списка текущих бронирований владельца
    @Test
    void findByOwnerShouldReturnEmptyCurrentBookings() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");

        Collection<BookingDto> bookings = bookingService.findByOwner(owner.getId(), BookingState.CURRENT);

        assertTrue(bookings.isEmpty());
    }

    // Проверка пустого списка завершенных бронирований владельца
    @Test
    void findByOwnerShouldReturnEmptyPastBookings() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");

        Collection<BookingDto> bookings = bookingService.findByOwner(owner.getId(), BookingState.PAST);

        assertTrue(bookings.isEmpty());
    }

    // Проверка получения ожидающих бронирований пользователя
    @Test
    void findByBookerShouldReturnWaitingBookings() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        UserDto booker = createUser("Пользователь2", "email2@mail.ru");
        ItemDto item = createItem(owner.getId(), "Вещь", "Описание вещи", true);
        bookingService.create(makeBooking(item.getId(), 1, 2), booker.getId());

        Collection<BookingDto> bookings = bookingService.findByBooker(booker.getId(), BookingState.WAITING);

        assertEquals(1, bookings.size());
    }

    // Проверка получения ожидающих бронирований владельца
    @Test
    void findByOwnerShouldReturnWaitingBookings() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        UserDto booker = createUser("Пользователь2", "email2@mail.ru");
        ItemDto item = createItem(owner.getId(), "Вещь", "Описание вещи", true);
        bookingService.create(makeBooking(item.getId(), 1, 2), booker.getId());

        Collection<BookingDto> bookings = bookingService.findByOwner(owner.getId(), BookingState.WAITING);

        assertEquals(1, bookings.size());
    }

    // Проверка получения отклоненных бронирований владельца
    @Test
    void findByOwnerShouldReturnRejectedBookings() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        UserDto booker = createUser("Пользователь2", "email2@mail.ru");
        ItemDto item = createItem(owner.getId(), "Вещь", "Описание вещи", true);
        BookingDto booking = bookingService.create(makeBooking(item.getId(), 1, 2), booker.getId());
        bookingService.approve(booking.getId(), owner.getId(), false);

        Collection<BookingDto> bookings = bookingService.findByOwner(owner.getId(), BookingState.REJECTED);

        assertEquals(1, bookings.size());
    }

    // Проверка получения отклоненных бронирований пользователя
    @Test
    void findByBookerShouldReturnRejectedBookings() {
        UserDto owner = createUser("Пользователь", "email@mail.ru");
        UserDto booker = createUser("Пользователь2", "email2@mail.ru");
        ItemDto item = createItem(owner.getId(), "Вещь", "Описание вещи", true);
        BookingDto booking = bookingService.create(makeBooking(item.getId(), 1, 2), booker.getId());
        bookingService.approve(booking.getId(), owner.getId(), false);

        Collection<BookingDto> bookings = bookingService.findByBooker(booker.getId(), BookingState.REJECTED);

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