package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.ConditionsNotMetException;

import java.util.Collection;

@RestController
@RequestMapping(path = "/bookings")
@Slf4j
@RequiredArgsConstructor
public class BookingController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingDto> create(@RequestHeader(USER_ID_HEADER) Long userId,
                                             @Valid @RequestBody BookingCreateDto bookingDto) {
        BookingDto createdBooking = bookingService.create(bookingDto, userId);

        log.info("Создано бронирование: id={}, пользователь id={}", createdBooking.getId(), userId);

        return ResponseEntity.ok(createdBooking);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingDto> approve(@RequestHeader(USER_ID_HEADER) Long userId,
                                              @PathVariable Long bookingId,
                                              @RequestParam Boolean approved) {
        BookingDto booking = bookingService.approve(bookingId, userId, approved);

        log.info("Рассмотрено бронирование: id={}, пользователь id={}, статус={}", bookingId, userId, booking.getStatus());

        return ResponseEntity.ok(booking);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDto> findById(@RequestHeader(USER_ID_HEADER) Long userId,
                                               @PathVariable Long bookingId) {
        BookingDto booking = bookingService.findById(bookingId, userId);

        log.info("Запрошено бронирование: id={}, пользователь id={}", bookingId, userId);

        return ResponseEntity.ok(booking);
    }

    @GetMapping
    public ResponseEntity<Collection<BookingDto>> findByBooker(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestParam(defaultValue = "ALL") String state) {
        BookingState bookingState = parseState(state);
        Collection<BookingDto> bookings = bookingService.findByBooker(userId, bookingState);

        log.info("Запрошен список бронирований пользователя: id={}, состояние={}", userId, bookingState);

        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/owner")
    public ResponseEntity<Collection<BookingDto>> findByOwner(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestParam(defaultValue = "ALL") String state) {
        BookingState bookingState = parseState(state);
        Collection<BookingDto> bookings = bookingService.findByOwner(userId, bookingState);

        log.info("Запрошен список бронирований вещей владельца: id={}, состояние={}", userId, bookingState);

        return ResponseEntity.ok(bookings);
    }

    private BookingState parseState(String state) {
        try {
            return BookingState.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new ConditionsNotMetException("Неизвестное состояние бронирования: " + state);
        }
    }
}