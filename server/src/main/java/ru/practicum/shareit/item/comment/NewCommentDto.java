package ru.practicum.shareit.item.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewCommentDto {
    private String text;
    private Long authorId;
    private LocalDateTime created;
}
