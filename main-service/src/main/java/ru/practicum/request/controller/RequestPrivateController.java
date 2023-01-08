package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.service.RequestService;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/requests")
@RequiredArgsConstructor
@Validated
@Slf4j
public class RequestPrivateController {

    private final RequestService requestService;

    @GetMapping
    public List<RequestDto> getAllByUserId(@PathVariable long userId) {
        log.info("RequestPrivateController: getAllByUserId.");

        return requestService.getAllByUserId(userId);
    }

    @PostMapping
    public RequestDto create(@PathVariable long userId, @RequestParam(value = "eventId") long eventId) {
        log.info("RequestPrivateController: create.");

        return requestService.create(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public RequestDto cancel(@Positive @PathVariable Long userId, @Positive @PathVariable Long requestId) {
        log.info("RequestPrivateController:  cancel.");

        return requestService.cancel(userId, requestId);
    }
}
