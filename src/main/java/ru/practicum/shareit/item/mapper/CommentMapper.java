package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.Comment;
import ru.practicum.shareit.item.dto.CommentDto;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(source = "author.name", target = "authorName")
    CommentDto toCommentDto(Comment comment);
}