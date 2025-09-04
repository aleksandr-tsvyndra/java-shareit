package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.NewCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoComplete;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

import java.util.Collection;

public interface ItemService {

    Collection<ItemDtoComplete> getAll(long userId);

    Collection<ItemDto> search(String text);

    ItemDtoComplete getById(long itemId);

    ItemDto create(long userId, NewItemDto dto);

    ItemDto update(long userId, long itemId, UpdateItemDto dto);

    void delete(long userId, long itemId);

    CommentDto addComment(long userId, long itemId, NewCommentDto dto);

}
