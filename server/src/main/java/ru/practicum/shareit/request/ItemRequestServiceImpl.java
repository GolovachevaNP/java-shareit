package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestAnswerDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ItemRequestDto create(ItemRequestDto requestDto, Long userId) {
        if (requestDto.getDescription() == null || requestDto.getDescription().isBlank()) {
            throw new ConditionsNotMetException("Описание запроса не должно быть пустым");
        }

        User requestor = getUserById(userId);

        ItemRequest request = new ItemRequest();
        request.setDescription(requestDto.getDescription());
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());

        return toDto(requestRepository.save(request));
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ItemRequestDto> findOwn(Long userId) {
        getUserById(userId);

        Collection<ItemRequest> requests = requestRepository.findByRequestorIdOrderByCreatedDesc(userId);
        Map<Long, List<ItemRequestAnswerDto>> answers = findAnswersByRequestIds(requests);

        return requests.stream()
                .map(request -> toDto(request, answers))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ItemRequestDto> findAllByOtherUsers(Long userId) {
        getUserById(userId);

        Collection<ItemRequest> requests = requestRepository.findByRequestorIdNotOrderByCreatedDesc(userId);
        Map<Long, List<ItemRequestAnswerDto>> answers = findAnswersByRequestIds(requests);

        return requests.stream()
                .map(request -> toDto(request, answers))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ItemRequestDto findById(Long requestId, Long userId) {
        getUserById(userId);

        return requestRepository.findById(requestId)
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Запрос с id = " + requestId + " не найден"));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
    }

    private ItemRequestDto toDto(ItemRequest request) {
        return toDto(request, findAnswersByRequestIds(List.of(request)));
    }

    private ItemRequestDto toDto(ItemRequest request, Map<Long, List<ItemRequestAnswerDto>> answers) {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        dto.setCreated(request.getCreated());
        dto.setItems(answers.getOrDefault(request.getId(), List.of()));
        return dto;
    }

    private Map<Long, List<ItemRequestAnswerDto>> findAnswersByRequestIds(Collection<ItemRequest> requests) {
        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        if (requestIds.isEmpty()) {
            return Map.of();
        }

        return itemRepository.findByRequestIdIn(requestIds).stream()
                .collect(Collectors.groupingBy(
                        item -> item.getRequest().getId(),
                        Collectors.mapping(this::toAnswerDto, Collectors.toList())
                ));
    }

    private ItemRequestAnswerDto toAnswerDto(Item item) {
        return new ItemRequestAnswerDto(item.getId(), item.getName(), item.getOwner().getId());
    }
}