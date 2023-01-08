package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.FullEventDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.ShortEventDto;
import ru.practicum.event.dto.UpdateEventRequest;
import ru.practicum.event.service.EventService;
import ru.practicum.request.dto.RequestDto;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Validated
@Slf4j
public class EventPrivateController {
    private final EventService eventService;

    @GetMapping
    public List<ShortEventDto> getAllPrivate(@PathVariable long userId,
                                             @PositiveOrZero @RequestParam(value = "from", defaultValue = "0") int from,
                                             @Positive @RequestParam(value = "size", defaultValue = "10") int size) {
        log.info("EventPrivateController: getAllPrivate.");

        return eventService.getAllPrivate(userId, from, size);
    }

    @PostMapping
    public FullEventDto createPrivate(@PathVariable long userId,
                                      @Valid @RequestBody NewEventDto eventDto) {
        log.info("EventPrivateController: createPrivate.");

        return eventService.createPrivate(userId, eventDto);
    }

    @PatchMapping
    public FullEventDto updatePrivate(@PathVariable long userId,
                                      @Valid @RequestBody UpdateEventRequest eventDto) {
        log.info("EventPrivateController: updatePrivate.");

        return eventService.updatePrivate(userId, eventDto);
    }

    @GetMapping("/{eventId}")
    public FullEventDto getByIdPrivate(@PathVariable long userId,
                                       @PathVariable long eventId) {
        log.info("EventPrivateController: getByIdPrivate.");

        return eventService.getByIdPrivate(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public FullEventDto cancelPrivate(@PathVariable long userId,
                                      @PathVariable long eventId) {
        log.info("EventPrivateController: cancelPrivate.");

        return eventService.cancelPrivate(userId, eventId);
    }

    @GetMapping("/{eventId}/requests")
    public List<RequestDto> getEventRequestsPrivate(@PathVariable long userId,
                                                    @PathVariable long eventId) {
        log.info("EventPrivateController: getEventRequestsPrivate.");

        return eventService.getEventRequestsPrivate(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests/{reqId}/confirm")
    public RequestDto confirmEventRequestPrivate(@PathVariable long userId,
                                                 @PathVariable long eventId,
                                                 @PathVariable long reqId) {
        log.info("EventPrivateController: confirmEventRequestPrivate.");

        return eventService.confirmEventRequestPrivate(userId, eventId, reqId);
    }

    @PatchMapping("/{eventId}/requests/{reqId}/reject")
    public RequestDto rejectEventRequestPrivate(@PathVariable long userId,
                                                @PathVariable long eventId,
                                                @PathVariable long reqId) {
        log.info("EventPrivateController: rejectEventRequestPrivate.");

        return eventService.rejectEventRequestPrivate(userId, eventId, reqId);
    }

}

