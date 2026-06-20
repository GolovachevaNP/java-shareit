package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

public interface ItemService {
    ItemDto create(ItemDto itemDto, Long userId);

    ItemDto update(Long itemId, ItemDto itemDto, Long userId);

    Collection<ItemDto> findByOwnerId(Long ownerId);

    ItemDto findById(Long itemId);

    Collection<ItemDto> search(String text);

    ItemDto findById(Long itemId, Long userId);

    CommentDto addComment(Long itemId, Long userId, CommentCreateDto commentDto);
}