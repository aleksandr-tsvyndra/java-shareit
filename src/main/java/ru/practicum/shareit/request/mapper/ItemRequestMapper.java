package ru.practicum.shareit.request.mapper;

import org.mapstruct.Mapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoShort;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

@Mapper(componentModel = "spring")
public interface ItemRequestMapper {

    ItemRequestDtoShort mapToItemRequestDtoShort(ItemRequest itemRequest);

    ItemRequestDto mapToItemRequestDto(ItemRequest itemRequest);

    ItemRequest mapToItemRequest(NewItemRequestDto newItemRequestDto);

}
