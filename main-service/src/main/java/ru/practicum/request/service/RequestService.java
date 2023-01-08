package ru.practicum.request.service;

import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.model.Request;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RequestService {

    List<RequestDto> getAllByUserId(long userId);

    List<RequestDto> getAllByEventId(Long eventId);

    Map<Long, Long> getCountConfirmedByEventsId(Set<Long> eventsId);

    Long getConfirmed(long eventId);

    Request getById(Long requestId);

    RequestDto create(long userId, long eventId);

    RequestDto cancel(Long userId, Long requestId);

    void save(Request request);

    void rejectAllByEventId(Long eventId);
}
