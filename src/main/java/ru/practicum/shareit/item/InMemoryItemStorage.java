package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class InMemoryItemStorage implements ItemStorage {

    private final Map<Long, Item> items = new LinkedHashMap<>();
    private long nextId = 1;

    @Override
    public Item create(Item item) {
        item.setId(nextId++);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public void update(String itemName, String itemDescription, Boolean itemAvailable, Long itemId) {
        Item item = items.get(itemId);
        if (item == null) {
            return;
        }
        if (itemName != null) {
            item.setName(itemName);
        }
        if (itemDescription != null) {
            item.setDescription(itemDescription);
        }
        if (itemAvailable != null) {
            item.setAvailable(itemAvailable);
        }
    }

    @Override
    public Collection<Item> findAll() {
        return items.values();
    }

    @Override
    public Collection<Item> findByOwnerId(Long ownerId) {
        return items.values().stream()
                .filter(item -> item.getOwner() != null)
                .filter(item -> item.getOwner().getId().equals(ownerId)).collect(Collectors.toList());
    }

    @Override
    public Collection<Item> search(String text) {
        if (text == null || text.isBlank()) {
            return java.util.List.of();
        }

        String searchText = text.toLowerCase();

        return items.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item -> item.getName().toLowerCase().contains(searchText)
                        || item.getDescription().toLowerCase().contains(searchText)).collect(Collectors.toList());
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public void deleteByOwnerId(Long ownerId) {
        items.values().removeIf(item -> item.getOwner() != null && item.getOwner().getId().equals(ownerId));
    }
}