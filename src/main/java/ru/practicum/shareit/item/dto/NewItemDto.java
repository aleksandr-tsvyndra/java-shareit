package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NewItemDto {
    @NotBlank(message = "Название вещи должно быть указано")
    private String name;
    @NotBlank(message = "Описание вещи должно быть указано")
    private String description;
    @NotNull(message = "Статус доступности вещи должен быть указан")
    private Boolean available;
}
