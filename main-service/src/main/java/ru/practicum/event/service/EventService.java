package ru.practicum.event.service;

import ru.practicum.event.dto.*;
import ru.practicum.event.model.Event;
import ru.practicum.request.dto.RequestDto;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface EventService {

    List<FullEventDto> getAllByAdmin(Long[] users, String[] states, Long[] categories,
                                     LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);

    FullEventDto updateByAdmin(long eventId, AdminUpdateEventRequest eventDto);

    FullEventDto publishByAdmin(long eventId);

    FullEventDto rejectByAdmin(long eventId);

    List<ShortEventDto> getAllPrivate(long userId, int from, int size);

    FullEventDto updatePrivate(long userId, UpdateEventRequest eventDto);

    FullEventDto createPrivate(long userId, NewEventDto eventDto);

    FullEventDto getByIdPrivate(long userId, long eventId);

    FullEventDto cancelPrivate(long userId, long eventId);

    List<RequestDto> getEventRequestsPrivate(long userId, long eventId);

    RequestDto confirmEventRequestPrivate(long userId, long eventId, long requestId);

    RequestDto rejectEventRequestPrivate(long userId, long eventId, long requestId);

    Event getById(long eventId);

    List<ShortEventDto> getAllPublic(String text,
                                     Long[] categories,
                                     Boolean paid,
                                     LocalDateTime rangeStart,
                                     LocalDateTime rangeEnd,
                                     Boolean onlyAvailable,
                                     String sort,
                                     Integer from,
                                     Integer size,
                                     HttpServletRequest request);

    FullEventDto getByIdPublic(long eventId);

    Set<Event> getAllByEvents(Set<Long> events);

    Map<Long, Integer> getHitCountsByEventIdSet(Set<Long> eventIds);
}
