package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/items")
@Slf4j
@RequiredArgsConstructor
public class ItemController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemDto> create(@RequestHeader(USER_ID_HEADER) Long userId,
                                          @Valid @RequestBody ItemDto itemDto) {
        ItemDto createdItem = itemService.create(itemDto, userId);
        log.info("Создана вещь: id={}, владелец id={}", createdItem.getId(), userId);
        return ResponseEntity.ok().body(createdItem);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> update(@RequestHeader(USER_ID_HEADER) Long userId,
                                          @PathVariable Long itemId,
                                          @RequestBody ItemDto itemDto) {
        ItemDto updatedItem = itemService.update(itemId, itemDto, userId);
        log.info("Обновлена вещь: id={}, владелец id={}", itemId, userId);
        return ResponseEntity.ok().body(updatedItem);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemResponseDto> getItem(@RequestHeader(USER_ID_HEADER) Long userId,
                                                   @PathVariable Long itemId) {
        ItemResponseDto item = (ItemResponseDto) itemService.findById(itemId, userId);
        log.info("Найдена вещь: id={}, название={}", item.getId(), item.getName());
        return ResponseEntity.ok().body(item);
    }

    @GetMapping
    public ResponseEntity<Collection<ItemResponseDto>> findByOwnerId(@RequestHeader(USER_ID_HEADER) Long userId) {
        Collection<ItemResponseDto> items = itemService.findByOwnerId(userId).stream()
                .map(ItemResponseDto.class::cast)
                .collect(Collectors.toList());
        log.info("Запрошен список вещей владельца id={}", userId);
        return ResponseEntity.ok().body(items);
    }

    @GetMapping("/search")
    public ResponseEntity<Collection<ItemDto>> search(@RequestParam String text) {
        Collection<ItemDto> items = itemService.search(text);
        log.info("Поиск вещей");
        return ResponseEntity.ok().body(items);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentDto> addComment(@RequestHeader(USER_ID_HEADER) Long userId,
                                                 @PathVariable Long itemId,
                                                 @Valid @RequestBody CommentCreateDto commentDto) {
        CommentDto comment = itemService.addComment(itemId, userId, commentDto);
        log.info("Добавлен комментарий: id={}, вещь id={}, автор id={}", comment.getId(), itemId, userId);
        return ResponseEntity.ok().body(comment);
    }
}