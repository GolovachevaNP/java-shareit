package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.Collection;

public interface BookingService {
    BookingDto create(BookingCreateDto bookingDto, Long userId);

    BookingDto approve(Long bookingId, Long userId, Boolean approved);

    BookingDto findById(Long bookingId, Long userId);

    Collection<BookingDto> findByBooker(Long userId, BookingState state);

    Collection<BookingDto> findByOwner(Long userId, BookingState state);
}