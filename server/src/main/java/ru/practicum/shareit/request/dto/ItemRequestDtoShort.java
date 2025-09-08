package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

@Data
@Builder
public class ItemRequestDtoShort {
    private Long id;
    private UserDto requestor;
    private String description;
    private LocalDateTime created;
}
