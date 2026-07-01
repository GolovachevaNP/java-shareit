package ru.practicum.shareit.item.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Collection;

@Data
@EqualsAndHashCode(callSuper = true)
public class ItemResponseDto extends ItemDto {
    private LocalDateTime lastBooking;
    private LocalDateTime nextBooking;
    private Collection<CommentDto> comments;
}