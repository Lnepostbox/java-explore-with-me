package ru.practicum.comments.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.comments.dto.UpdateCommentDto;
import ru.practicum.comments.service.CommentsService;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/users/{userId}/comments")
@RequiredArgsConstructor
public class CommentPrivateController {
    private final CommentsService commentService;

    @GetMapping("/{commentId}")
    public CommentDto getByIdPrivate(@PathVariable long userId,
                                     @PathVariable long commentId) {
        log.info("CommentPrivateController: getByIdPrivate.");

        return commentService.getByIdPrivate(userId, commentId);
    }

    @PostMapping
    public CommentDto createPrivate(@PathVariable(value = "userId") @Positive long userId,
                                    @RequestBody @Valid NewCommentDto newCommentDto) {
        log.info("CommentPrivateController: createPrivate.");

        return commentService.createPrivate(newCommentDto, userId);
    }

    @GetMapping
    public List<CommentDto> getAllPrivate(@PathVariable long userId,
                                          @PositiveOrZero @RequestParam(value = "from", defaultValue = "0") int from,
                                          @Positive @RequestParam(value = "size", defaultValue = "10") int size) {
        log.info("CommentPrivateController: getAllPrivate.");

        return commentService.getAllPrivate(userId, PageRequest.of(from / size, size));
    }

    @PatchMapping("/{commentId}")
    public CommentDto updatePrivate(@PathVariable long userId,
                                    @PathVariable long commentId,
                                    @Valid @RequestBody UpdateCommentDto updateCommentDto) {
        log.info("CommentPrivateController: updatePrivate.");

        return commentService.updatePrivate(userId, commentId, updateCommentDto);
    }

    @DeleteMapping("/{commentId}")
    public void deletePrivate(@PathVariable long userId,
                              @PathVariable long commentId) {
        log.info("CommentPrivateController: deletePrivate.");

        commentService.deletePrivate(userId, commentId);
    }
}