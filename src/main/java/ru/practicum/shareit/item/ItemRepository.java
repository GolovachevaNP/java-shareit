package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Collection<Item> findByOwnerId(Long ownerId);

    void deleteByOwnerId(Long ownerId);

    @Query("select i from Item i "
            + "where i.available = true "
            + "and (upper(i.name) like upper(concat('%', ?1, '%')) "
            + "or upper(i.description) like upper(concat('%', ?1, '%')))")
    Collection<Item> search(String text);
}