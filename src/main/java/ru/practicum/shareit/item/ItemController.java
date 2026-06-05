package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

@RestController
@RequestMapping("/items")
@Slf4j
@RequiredArgsConstructor
public class ItemController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemService itemService;

    @PostMapping
    public ItemDto create(@RequestHeader(USER_ID_HEADER) Long userId,
                          @Valid @RequestBody ItemDto itemDto) {
        ItemDto createdItem = itemService.create(itemDto, userId);
        log.info("Создана вещь: id={}, владелец id={}", createdItem.getId(), userId);
        return createdItem;
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(USER_ID_HEADER) Long userId,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto itemDto) {
        ItemDto updatedItem = itemService.update(itemId, itemDto, userId);
        log.info("Обновлена вещь: id={}, владелец id={}", itemId, userId);
        return updatedItem;
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@PathVariable Long itemId) {
        ItemDto item = itemService.findById(itemId);
        log.info("Найдена вещь: id={}, название={}", item.getId(), item.getName());
        return item;
    }

    @GetMapping
    public Collection<ItemDto> findByOwnerId(@RequestHeader(USER_ID_HEADER) Long userId) {
        Collection<ItemDto> items = itemService.findByOwnerId(userId);
        log.info("Запрошен список вещей владельца id={}", userId);
        return items;
    }

    @GetMapping("/search")
    public Collection<ItemDto> search(@RequestParam String text) {
        Collection<ItemDto> items = itemService.search(text);
        log.info("Поиск вещей");
        return items;
    }
}