package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoShort;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

import java.util.Collection;

public interface ItemRequestService {

    Collection<ItemRequestDtoShort> getAll(int from, int size);

    Collection<ItemRequestDto> getAllByRequestorId(long requestorId, int from, int size);

    ItemRequestDto getById(long requestId);

    ItemRequestDtoShort create(long requestorId, NewItemRequestDto dto);

}
