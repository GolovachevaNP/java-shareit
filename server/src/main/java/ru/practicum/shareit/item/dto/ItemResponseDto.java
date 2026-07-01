package ru.practicum.shareit.item.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;

@Data
@EqualsAndHashCode(callSuper = true)
public class ItemResponseDto extends ItemDto {
    private ItemBookingDto lastBooking;
    private ItemBookingDto nextBooking;
    private Collection<CommentDto> comments;
}