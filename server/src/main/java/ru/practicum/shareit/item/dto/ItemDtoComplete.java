package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

@Data
@Builder
public class ItemDtoComplete {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private UserDto sharer;
    private BookingDtoShort lastBooking;
    private BookingDtoShort nextBooking;
    private Collection<CommentDto> comments;
    private ItemRequestDto request;
}
