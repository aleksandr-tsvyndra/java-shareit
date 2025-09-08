package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateItemDto {
    private Long itemId;
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
