package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Optional;

public interface ItemRepository {

    Collection<Item> findAll();

    Optional<Item> findById(long itemId);

    Item create(Item newItem);

    Item update(Item updateItem);

    void delete(long itemId);

}
