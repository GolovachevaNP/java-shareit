package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingDto create(BookingCreateDto bookingDto, Long userId) {
        validateBookingDates(bookingDto);

        User booker = getUserById(userId);
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь с id = " + bookingDto.getItemId() + " не найдена"));

        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Владелец не может забронировать свою вещь");
        }
        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new ConditionsNotMetException("Вещь недоступна для бронирования");
        }

        Booking booking = new Booking();
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking createdBooking = bookingRepository.save(booking);

        log.debug("Создано бронирование: id={}, пользователь id={}, вещь id={}", createdBooking.getId(), userId,
                item.getId());

        return bookingMapper.toBookingDto(createdBooking);
    }

    @Override
    @Transactional
    public BookingDto approve(Long bookingId, Long userId, Boolean approved) {
        if (approved == null) {
            throw new ConditionsNotMetException("Необходимо указать решение по бронированию");
        }

        Booking booking = getBookingById(bookingId);

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Пользователь с id = " + userId + " не владелец вещи");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ConditionsNotMetException("Бронирование уже рассмотрено");
        }

        booking.setStatus(Boolean.TRUE.equals(approved) ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = bookingRepository.save(booking);

        log.debug("Изменен статус бронирования: id={}, статус={}", bookingId, updatedBooking.getStatus());

        return bookingMapper.toBookingDto(updatedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDto findById(Long bookingId, Long userId) {
        getUserById(userId);
        Booking booking = getBookingById(bookingId);

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);
        if (!isBooker && !isOwner) {
            throw new NotFoundException("Бронирование с id = " + bookingId + " недоступно пользователю");
        }

        log.debug("Получение бронирования: id={}", bookingId);

        return bookingMapper.toBookingDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<BookingDto> findByBooker(Long userId, BookingState state) {
        getUserById(userId);
        LocalDateTime now = LocalDateTime.now();

        log.debug("Получение списка бронирований пользователя: id={}, состояние={}", userId, state);

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByBookerIdOrderByStartDesc(userId);
            case CURRENT -> bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                    userId, now, now);
            case PAST -> bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(userId, now);
            case FUTURE -> bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(userId, now);
            case WAITING -> bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                    userId, BookingStatus.WAITING);
            case REJECTED -> bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                    userId, BookingStatus.REJECTED);
        };

        return bookings.stream()
                .map(bookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<BookingDto> findByOwner(Long userId, BookingState state) {
        getUserById(userId);
        LocalDateTime now = LocalDateTime.now();

        log.debug("Получение списка бронирований вещей владельца: id={}, состояние={}", userId, state);

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByItemOwnerIdOrderByStartDesc(userId);
            case CURRENT -> bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                    userId, now, now);
            case PAST -> bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, now);
            case FUTURE -> bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(userId, now);
            case WAITING -> bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                    userId, BookingStatus.WAITING);
            case REJECTED -> bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                    userId, BookingStatus.REJECTED);
        };

        return bookings.stream()
                .map(bookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    private void validateBookingDates(BookingCreateDto bookingDto) {
        if (bookingDto.getStart() == null || bookingDto.getEnd() == null) {
            throw new ConditionsNotMetException("Необходимо указать даты начала и окончания бронирования");
        }
        if (!bookingDto.getStart().isAfter(LocalDateTime.now())) {
            throw new ConditionsNotMetException("Дата начала бронирования должна быть в будущем");
        }
        if (!bookingDto.getEnd().isAfter(bookingDto.getStart())) {
            throw new ConditionsNotMetException("Дата окончания бронирования должна быть позже даты начала");
        }
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
    }

    private Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id = " + bookingId + " не найдено"));
    }
}