package ru.practicum.request.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.State;
import ru.practicum.event.service.EventService;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.dto.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.Status;
import ru.practicum.request.storage.RequestRepository;
import ru.practicum.user.service.UserService;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor_ = {@Lazy})
@Slf4j
public class RequestServiceImpl implements RequestService {

    private final EventService eventService;
    private final UserService userService;
    private final RequestRepository requestRepository;

    @Override
    public List<RequestDto> getAllByUserId(long userId) {
        List<Request> requests = requestRepository.findAllByRequesterId(userId);
        log.info("RequestService: getAllByUserId. UserId {}.", userId);

        return requests.stream()
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RequestDto> getAllByEventId(Long eventId) {
        log.info("RequestService: getAllByEventId. EventId {}.", eventId);

        return requestRepository.findAllByEventId(eventId).stream()
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public Map<Long, Long> getCountConfirmedByEventsId(Set<Long> eventsId) {
        Map<Long, Long> results = new HashMap<>();
        List<Object[]> countList = requestRepository.getCountConfirmedByEventIdList(eventsId);
        for (Object[] count: countList) {
            results.put(((BigInteger) count[0]).longValue(), ((BigInteger) count[1]).longValue());
        }
        log.info("RequestService: getCountConfirmedByEventsId.");
        return results;
    }

    @Override
    public Request getById(Long requestId) {
        log.info("RequestService: getById. RequestId {}.", requestId);

        return requestRepository.findById(requestId).orElseThrow(() ->
                new NotFoundException("Request {} not found", requestId));
    }

    @Override
    public Long getConfirmed(long eventId) {
        log.info("RequestService: getConfirmed. EventId {}.", eventId);

        return requestRepository.getCountConfirmedByEventId(eventId);
    }

    @Override
    public RequestDto create(long userId, long eventId) {
        Event event = eventService.getById(eventId);
        userService.getById(userId);
        Optional<Request> requestOptional = requestRepository.findByRequesterIdAndEventId(userId, eventId);
        if (requestOptional.isPresent()) {
            log.info("Request is already exist.");

            return RequestMapper.toRequestDto(requestOptional.get());
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new ValidationException("Request for own event is forbidden.");
        }
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new ValidationException("Request for unpublished event is forbidden.");
        }
        if (getConfirmed(eventId).equals((long) event.getParticipantLimit()) && event.getParticipantLimit() != 0) {
            throw new ValidationException("Request for event with reached participation limit is forbidden.");
        }
        Status status;
        if (event.getRequestModeration()) {
            status = Status.PENDING;
        } else {
            status = Status.CONFIRMED;
        }
        Request request = Request.builder()
                .status(status)
                .event(event)
                .requester(userService.getById(userId))
                .created(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS))
                .build();
        log.info("RequestService: create. UserId {}, eventId {}.", userId, eventId);

        return RequestMapper.toRequestDto(requestRepository.save(request));
    }

    @Override
    public RequestDto cancel(Long userId, Long requestId) {
        Optional<Request> request = requestRepository.findByRequesterIdAndId(userId, requestId);
        if (request.isEmpty()) {
            throw new ValidationException("Request doesn't exist.");
        }
        request.get().setStatus(Status.CANCELED);
        log.info("RequestService: cancel. RequestId {}.", requestId);

        return RequestMapper.toRequestDto(requestRepository.save(request.get()));
    }

    @Override
    public void save(Request request) {
        log.info("RequestService: save. Request {}.", request);
        requestRepository.save(request);
    }

    @Override
    public void rejectAllByEventId(Long eventId) {
        log.info("RequestService: rejectAllByEventId. EventId {}.", eventId);
        requestRepository.rejectAll(eventId);
    }

}
