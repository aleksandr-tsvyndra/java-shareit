package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NewCommentDto {
    @NotBlank(message = "Отзыв не может быть пустым")
    private String text;
    private Long authorId;
}
