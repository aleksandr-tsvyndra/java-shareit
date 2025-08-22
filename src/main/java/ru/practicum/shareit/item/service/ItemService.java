package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

import java.util.Collection;

public interface ItemService {

    Collection<ItemDto> getAll(long userId);

    Collection<ItemDto> search(String text);

    ItemDto getById(long itemId);

    ItemDto create(long userId, NewItemDto dto);

    ItemDto update(long userId, long itemId, UpdateItemDto dto);

    void delete(long userId, long itemId);

}
