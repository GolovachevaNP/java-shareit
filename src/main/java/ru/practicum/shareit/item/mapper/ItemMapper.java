package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    @Mapping(source = "owner.id", target = "ownerId")
    @Mapping(source = "request.id", target = "requestId")
    ItemDto toItemDto(Item item);

    @Mapping(source = "owner.id", target = "ownerId")
    @Mapping(source = "request.id", target = "requestId")
    @Mapping(target = "lastBooking", ignore = true)
    @Mapping(target = "nextBooking", ignore = true)
    @Mapping(target = "comments", ignore = true)
    ItemResponseDto toItemResponseDto(Item item);

    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "request", ignore = true)
    Item toItem(ItemDto itemDto);
}