package ru.practicum.comments.service;

import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.comments.dto.UpdateCommentDto;
import java.time.LocalDateTime;
import java.util.List;

public interface CommentsService {

    List<CommentDto> getAllByUserId(long userId);

    List<CommentDto> getAllByEventId(long eventId);

    List<CommentDto> getAllPrivate(long userId, int from, int size);

    CommentDto getByIdPrivate(long userId, long commentId);

    CommentDto createPrivate(NewCommentDto comment, long userId);

    CommentDto updatePrivate(long userId, long commentId, UpdateCommentDto updateCommentDto);

    void deletePrivate(long userId, long commentId);

    List<CommentDto> getAllAdmin(Long[] users, Long[] events,
                                 LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);

    CommentDto updateAdmin(long commentId, UpdateCommentDto updateCommentDto);
}