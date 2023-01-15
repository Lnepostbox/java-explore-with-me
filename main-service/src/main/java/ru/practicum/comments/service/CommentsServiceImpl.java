package ru.practicum.comments.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.CommentMapper;
import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.comments.dto.UpdateCommentDto;
import ru.practicum.comments.model.Comment;
import ru.practicum.comments.storage.CommentRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.service.EventService;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;
import ru.practicum.util.Constant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
@Transactional
public class CommentsServiceImpl implements CommentsService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final EventService eventService;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getAllByUserId(long userId) {
        log.info("CommentsService: getAllByUserId. UserId {}.", userId);

        return commentRepository.getAllByUserId(userId).stream()
                .map(commentMapper::fromComment).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getAllByEventId(long eventId) {
        log.info("CommentsService: getAllByEventId. EventId {}.", eventId);

        return commentRepository.getAllByEventId(eventId).stream()
                .map(commentMapper::fromComment).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getAllPrivate(long userId, Pageable pageable) {
        List<Comment> comments = commentRepository.findAllByUserId(userId, pageable);
        log.info("CommentsService: getAllPrivate.");

        return comments.stream().map(commentMapper::fromComment).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDto getByIdPrivate(long userId, long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new NotFoundException("Comment {} doesn't exist.", commentId));
        if (userId != comment.getUser().getId()) {
            throw new BadRequestException("Comment was made by another user.");
        }
        log.info("CommentsService: getByIdPrivate. CommentId {}.", commentId);

        return commentMapper.fromComment(comment);
    }

    @Override
    public CommentDto createPrivate(NewCommentDto newCommentDto, long userId) {
        User user = userService.getById(userId);
        Event event = eventService.getEventByIdPrivate(newCommentDto.getEventId());
        log.info("CommentsService: createPrivate.");
        return commentMapper.fromComment(
                commentRepository.save(commentMapper.fromNewCommentDto(newCommentDto, user, event))
        );
    }

    @Override
    public CommentDto updatePrivate(long userId, long commentId, UpdateCommentDto updateCommentDto) {
        Optional<Comment> comment = commentRepository.findByIdAndUserId(userId, commentId);
        if (comment.isEmpty()) {
            throw new BadRequestException("Comment with commentId and userId doesn't exist.");
        }
        if (userId != comment.get().getUser().getId()) {
            throw new BadRequestException("Comment can be updated only by author or administrator.");
        }
        comment.get().setText(updateCommentDto.getText());
        log.info("CommentsService: updatePrivate. CommentId {}.", commentId);

        return commentMapper.fromComment(commentRepository.save(comment.get()));
    }

    @Override
    public void deletePrivate(long userId, long commentId) {
        Optional<Comment> comment = commentRepository.findByIdAndUserId(commentId, userId);
        if (comment.isEmpty()) {
            throw new BadRequestException("Comment with commentId and userId doesn't exist.");
        }
        commentRepository.deleteById(commentId);
        log.info("CommentsService: delete. CommentId {}.", commentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getAllAdmin(Long[] users, Long[] events,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageable) {
        LocalDateTime startDate = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        LocalDateTime endDate = LocalDateTime.parse("5000-01-01 00:00:00",
                DateTimeFormatter.ofPattern(Constant.DATE_TIME_STRING));
        if (rangeStart != null) {
            startDate = rangeStart;
        }
        if (rangeEnd != null) {
            endDate = rangeEnd;
        }
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Start can't be after End.");
        }
        List<Comment> comments = commentRepository.findAllByUsersAndEvents(users, events,
                startDate, endDate, pageable);
        log.info("CommentsService: getAllAdmin.");

        return comments.stream()
                .map(commentMapper::fromComment)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto updateAdmin(long commentId, UpdateCommentDto updateCommentDto) {
        Optional<Comment> comment = commentRepository.findById(commentId);
        if (comment.isEmpty()) {
            throw new BadRequestException("Comment with commentId and userId doesn't exist.");
        }
        comment.get().setText(updateCommentDto.getText());
        log.info("CommentsService: updateAdmin. CommentId {}.", commentId);

        return commentMapper.fromComment(commentRepository.save(comment.get()));
    }
}
