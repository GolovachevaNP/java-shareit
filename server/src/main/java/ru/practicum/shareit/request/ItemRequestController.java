package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Collection;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final ItemRequestService requestService;

    @PostMapping
    public ResponseEntity<ItemRequestDto> create(@RequestHeader(USER_ID_HEADER) Long userId,
                                                 @Valid @RequestBody ItemRequestDto requestDto) {
        return ResponseEntity.ok(requestService.create(requestDto, userId));
    }

    @GetMapping
    public ResponseEntity<Collection<ItemRequestDto>> findOwn(@RequestHeader(USER_ID_HEADER) Long userId) {
        return ResponseEntity.ok(requestService.findOwn(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<Collection<ItemRequestDto>> findAllByOtherUsers(@RequestHeader(USER_ID_HEADER) Long userId) {
        return ResponseEntity.ok(requestService.findAllByOtherUsers(userId));
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ItemRequestDto> findById(@RequestHeader(USER_ID_HEADER) Long userId,
                                                   @PathVariable Long requestId) {
        return ResponseEntity.ok(requestService.findById(requestId, userId));
    }
}
