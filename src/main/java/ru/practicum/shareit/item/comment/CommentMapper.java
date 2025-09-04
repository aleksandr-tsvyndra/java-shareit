package ru.practicum.shareit.item.comment;

public interface CommentMapper {

    Comment mapToComment(NewCommentDto newCommentDto);

    CommentDto mapToCommentDto(Comment comment);
}
