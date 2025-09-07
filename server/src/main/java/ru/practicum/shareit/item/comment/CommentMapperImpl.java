package ru.practicum.shareit.item.comment;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CommentMapperImpl implements CommentMapper {

    @Override
    public Comment mapToComment(NewCommentDto newCommentDto) {
        if (newCommentDto == null) {
            return null;
        } else {
            Comment comment = new Comment();
            comment.setText(newCommentDto.getText());
            comment.setCreated(LocalDateTime.now());
            return comment;
        }
    }

    @Override
    public CommentDto mapToCommentDto(Comment comment) {
        if (comment == null) {
            return null;
        } else {
            CommentDto commentDto = new CommentDto();
            commentDto.setId(comment.getId());
            commentDto.setText(comment.getText());
            commentDto.setAuthorName(comment.getAuthor().getName());
            commentDto.setCreated(comment.getCreated());
            return commentDto;
        }
    }
}
