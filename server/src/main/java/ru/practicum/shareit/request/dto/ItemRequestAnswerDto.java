package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemRequestAnswerDto {
    private Long id;
    private String name;
    private Long ownerId;
}