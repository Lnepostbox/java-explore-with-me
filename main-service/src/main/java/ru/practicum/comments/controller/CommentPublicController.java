package ru.practicum.comments.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.service.CommentsService;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/comments")
@Validated
@RequiredArgsConstructor
public class CommentPublicController {
    private final CommentsService commentService;

    @GetMapping("/events/{eventId}")
    public List<CommentDto> getAllByEventId(@PathVariable long eventId) {
        log.info("CommentPublicController: getAllByEventId.");

        return commentService.getAllByEventId(eventId);
    }
}
