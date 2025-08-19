package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UpdateItemDto {
    @NotNull(message = "Id вещи должно быть указано")
    @Positive(message = "Id должно быть положительным числом")
    private Long id;
    private String name;
    private String description;
    private Boolean available;

    public boolean hasName() {
        return ! (name == null || name.trim().isBlank());
    }

    public boolean hasDescription() {
        return ! (description == null || description.trim().isBlank());
    }

    public boolean hasAvailable() {
        return available != null;
    }

}
