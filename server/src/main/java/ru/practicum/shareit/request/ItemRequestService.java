package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Collection;

public interface ItemRequestService {
    ItemRequestDto create(ItemRequestDto requestDto, Long userId);

    Collection<ItemRequestDto> findOwn(Long userId);

    Collection<ItemRequestDto> findAllByOtherUsers(Long userId);

    ItemRequestDto findById(Long requestId, Long userId);
}