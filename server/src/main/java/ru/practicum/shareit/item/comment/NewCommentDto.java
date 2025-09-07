package ru.practicum.shareit.item.comment;

import lombok.Data;

@Data
public class NewCommentDto {
    private String text;
    private Long authorId;
}
