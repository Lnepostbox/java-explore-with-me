package ru.practicum.comments.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.UpdateCommentDto;
import ru.practicum.comments.service.CommentsService;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@Validated
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class CommentAdminController {
    private final CommentsService commentService;

    @GetMapping("/users/{userId}")
    public List<CommentDto> getAllByUserId(@PathVariable long userId) {
        log.info("CommentAdminController: getAllByUserId.");

        return commentService.getAllByUserId(userId);
    }

    @GetMapping("/events/{eventId}")
    public List<CommentDto> getAllByEventId(@PathVariable long eventId) {
        log.info("CommentAdminController: getAllByEventId.");

        return commentService.getAllByEventId(eventId);
    }

    @GetMapping
    public List<CommentDto> getAllAdmin(
            @RequestParam(required = false) Long[] users,
            @RequestParam(required = false) Long[] events,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @PositiveOrZero @RequestParam(defaultValue = "0") int from,
            @Positive @RequestParam(defaultValue = "10") int size) {
        log.info("CommentAdminController: getAllAdmin.");

        return commentService.getAllAdmin(users, events, rangeStart, rangeEnd, from, size);
    }

    @PutMapping("/{commentId}")
    public CommentDto updateAdmin(@RequestBody UpdateCommentDto updateCommentDto,
                                  @PathVariable long commentId) {
        log.info("CommentAdminController: updateAdmin.");

        return commentService.updateAdmin(commentId, updateCommentDto);
    }
}