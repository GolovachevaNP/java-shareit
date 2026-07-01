package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final ItemRequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(USER_ID_HEADER) Long userId,
                                         @Valid @RequestBody ItemRequestDto requestDto) {
        log.info("Создание запроса вещи {}, userId={}", requestDto, userId);
        return requestClient.create(userId, requestDto);
    }

    @GetMapping
    public ResponseEntity<Object> findOwn(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Получение запросов вещей пользователя, userId={}", userId);
        return requestClient.findOwn(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> findAllByOtherUsers(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Получение запросов вещей других пользователей, userId={}", userId);
        return requestClient.findAllByOtherUsers(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> findById(@RequestHeader(USER_ID_HEADER) Long userId,
                                           @PathVariable Long requestId) {
        log.info("Получение запроса вещи {}, userId={}", requestId, userId);
        return requestClient.findById(userId, requestId);
    }
}