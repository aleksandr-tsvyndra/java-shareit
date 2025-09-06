package ru.practicum.shareit.request.dto;

import lombok.Data;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

@Data
public class ItemRequestDtoShort {
    private Long id;
    private UserDto requestor;
    private String description;
    private LocalDateTime created;
}
