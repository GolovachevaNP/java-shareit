package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    private void validateItem(Item item) {
        if (item.getName() == null || item.getName().isBlank()) {
            log.warn("Некорректное название вещи");
            throw new ConditionsNotMetException("Название вещи не должно быть пустым");
        }
        if (item.getDescription() == null || item.getDescription().isBlank()) {
            log.warn("Некорректное описание вещи");
            throw new ConditionsNotMetException("Описание вещи не должно быть пустым");
        }
        if (item.getAvailable() == null) {
            log.warn("Не указан статус доступности вещи");
            throw new ConditionsNotMetException("Необходимо указать статус доступности вещи");
        }
    }

    private void validateItemForUpdate(Item item) {
        if (item.getName() != null && item.getName().isBlank()) {
            log.warn("Некорректное название вещи");
            throw new ConditionsNotMetException("Название вещи не должно быть пустым");
        }
        if (item.getDescription() != null && item.getDescription().isBlank()) {
            log.warn("Некорректное описание вещи");
            throw new ConditionsNotMetException("Описание вещи не должно быть пустым");
        }
    }

    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }

    private void validateItemExists(Long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new NotFoundException("Вещь с id = " + itemId + " не найдена");
        }
    }

    private void validateOwner(Item item, Long userId) {
        if (item.getOwner() == null || !item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не владелец вещи");
        }
    }

    private Item getItemById(Long itemId) {
        log.debug("Получение вещи: id={}", itemId);
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id = " + itemId + " не найдена"));
    }

    @Override
    @Transactional
    public ItemDto create(ItemDto itemDto, Long userId) {
        Item item = itemMapper.toItem(itemDto);

        validateItem(item);

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        item.setOwner(owner);

        Item createdItem = itemRepository.save(item);

        log.debug("Создана вещь: id={}, владелец id={}", createdItem.getId(), userId);

        return itemMapper.toItemDto(createdItem);
    }

    @Override
    @Transactional
    public ItemDto update(Long itemId, ItemDto itemDto, Long userId) {
        Item updatedItem = itemMapper.toItem(itemDto);

        validateUserExists(userId);
        validateItemExists(itemId);

        Item savedItem = getItemById(itemId);
        validateOwner(savedItem, userId);
        validateItemForUpdate(updatedItem);

        if (updatedItem.getName() != null) {
            savedItem.setName(updatedItem.getName());
        }
        if (updatedItem.getDescription() != null) {
            savedItem.setDescription(updatedItem.getDescription());
        }
        if (updatedItem.getAvailable() != null) {
            savedItem.setAvailable(updatedItem.getAvailable());
        }

        Item result = itemRepository.save(savedItem);

        log.debug("Обновлена информация о вещи: id={}", itemId);

        return itemMapper.toItemDto(result);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ItemDto> findByOwnerId(Long ownerId) {
        validateUserExists(ownerId);

        log.debug("Получение списка вещей владельца id={}", ownerId);

        LocalDateTime now = LocalDateTime.now();

        return itemRepository.findByOwnerId(ownerId).stream()
                .map(item -> toItemDtoWithBookingDates(item, now))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto findById(Long itemId) {
        return toItemDtoWithComments(getItemById(itemId));
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto findById(Long itemId, Long userId) {
        validateUserExists(userId);
        Item item = getItemById(itemId);
        if (item.getOwner() != null && item.getOwner().getId().equals(userId)) {
            return toItemDtoWithBookingDates(item, LocalDateTime.now());
        }
        return toItemDtoWithComments(item);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ItemDto> search(String text) {
        log.debug("Поиск вещей");

        if (text == null || text.isBlank()) {
            return List.of();
        }

        return itemRepository.search(text).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long itemId, Long userId, CommentCreateDto commentDto) {
        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new ConditionsNotMetException("Текст отзыва не должен быть пустым");
        }

        Item item = getItemById(itemId);
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        LocalDateTime now = LocalDateTime.now();

        boolean userBookedItem = bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(itemId, userId,
                BookingStatus.APPROVED, now);

        if (!userBookedItem) {
            throw new ConditionsNotMetException("Оставить отзыв о вещи может лишь пользователь, завершивший её аренду");
        }

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(now);

        Comment savedComment = commentRepository.save(comment);

        log.debug("Добавлен отзыв: id={}, вещь id={}, автор id={}", savedComment.getId(), itemId, userId);

        return commentMapper.toCommentDto(savedComment);
    }

    private ItemResponseDto toItemDtoWithBookingDates(Item item, LocalDateTime now) {
        ItemResponseDto itemDto = toItemDtoWithComments(item);

        bookingRepository.findFirstByItemIdAndStatusAndEndBeforeOrderByEndDesc(item.getId(), BookingStatus.APPROVED, now)
                .ifPresent(booking -> itemDto.setLastBooking(booking.getEnd()));

        bookingRepository.findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(item.getId(), BookingStatus.APPROVED, now)
                .ifPresent(booking -> itemDto.setNextBooking(booking.getStart()));

        return itemDto;
    }

    private ItemResponseDto toItemDtoWithComments(Item item) {
        ItemResponseDto itemDto = itemMapper.toItemResponseDto(item);
        itemDto.setComments(commentRepository.findByItemIdOrderByCreatedAsc(item.getId()).stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList()));
        return itemDto;
    }
}