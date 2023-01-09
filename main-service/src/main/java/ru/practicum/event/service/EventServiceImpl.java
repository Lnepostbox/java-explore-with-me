package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.CategoryMapper;
import ru.practicum.category.service.CategoryService;
import ru.practicum.event.client.EventClient;
import ru.practicum.event.dto.*;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.State;
import ru.practicum.event.storage.EventRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.dto.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.Status;
import ru.practicum.request.service.RequestService;
import ru.practicum.user.service.UserService;
import ru.practicum.util.PageableRequest;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {
    private final CategoryService categoryService;
    private final EventRepository repository;
    private final UserService userService;
    private final RequestService requestService;
    private final EventClient eventClient;
    private final EventMapper eventMapper;

    private static final String DATE_TIME_STRING = "yyyy-MM-dd HH:mm:ss";

    @Override
    public List<FullEventDto> getAllByAdmin(Long[] users, String[] states, Long[] categories,
                                            LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        List<State> stateList = new ArrayList<>();
        if (states != null) {
            for (String state : states) {
                State stateCorrect = State.from(state)
                        .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + state));
                stateList.add(stateCorrect);
            }
        }
        List<LocalDateTime> ranges = eventDatePreparation(rangeStart, rangeEnd);
        Sort sort = Sort.sort(Event.class).by(Event::getEventDate).descending();
        List<Event> events = repository.findAllByUsersAndStatesAndCategories(users, stateList, categories,
                ranges.get(0), ranges.get(1), getPageable(from, size, sort));
        Set<Long> eventIds = events.stream().map(eventMapper::toId).collect(Collectors.toSet());
        Map<Long, Long> confirmedRequests = requestService.getCountConfirmedByEventsId(eventIds);
        Map<Long, Integer> hitCounts = getHitCountsByEventIdSet(eventIds);
        log.info("EventService: getAllByAdmin.");

        return events.stream()
                .map((Event event) -> eventMapper.toFullDto(event,
                        confirmedRequests.get(event.getId()),
                        hitCounts.get(event.getId()))
                )
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FullEventDto updateByAdmin(long eventId, AdminUpdateEventRequest eventDto) {
        Event event = getByIdAndThrow(eventId);
        eventUpdatePreparation(eventMapper.fromAdminUpdateEventRequest(eventDto), event);
        Optional.ofNullable(eventDto.getLocation()).ifPresent(event::setLocation);
        Optional.ofNullable(eventDto.getRequestModeration()).ifPresent(event::setRequestModeration);
        Map<Long, Integer> hitCounts = getHitCountsByEventIdSet(Set.of(eventId));
        log.info("EventService: updateByAdmin. EventId {}.", eventId);

        return eventMapper.toFullDto(event,
                requestService.getConfirmed(eventId),
                hitCounts.get(event.getId()));
    }

    @Override
    @Transactional
    public FullEventDto publishByAdmin(long eventId) {
        Event event = getByIdAndThrow(eventId);
        if (event.getEventDate().plusHours(1).isAfter(LocalDateTime.now()) &&
                event.getState() == State.PENDING) {
            event.setPublishedOn(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));
            event.setState(State.PUBLISHED);
            log.info("EventService: publishByAdmin. EventId {}.", eventId);
        }
        Map<Long, Integer> hitCounts = getHitCountsByEventIdSet(Set.of(eventId));

        return eventMapper.toFullDto(event,
                requestService.getConfirmed(eventId),
                hitCounts.get(event.getId()));
    }

    @Override
    @Transactional
    public FullEventDto rejectByAdmin(long eventId) {
        Event event = getByIdAndThrow(eventId);
        if (event.getState() == State.PENDING) {
            event.setState(State.CANCELED);
            log.info("EventService: rejectByAdmin. EventId {}.", eventId);
        }
        Map<Long, Integer> hitCounts = getHitCountsByEventIdSet(Set.of(eventId));

        return eventMapper.toFullDto(event,
                requestService.getConfirmed(eventId),
                hitCounts.get(event.getId()));
    }

    @Override
    public List<ShortEventDto> getAllPrivate(long userId, int from, int size) {
        List<Event> events = repository.findAllByInitiatorId(userId, getPageable(from, size, Sort.unsorted()));
        Set<Long> eventIds = events.stream().map(eventMapper::toId).collect(Collectors.toSet());
        Map<Long, Long> confirmedRequests = requestService.getCountConfirmedByEventsId(eventIds);
        Map<Long, Integer> hitCounts = getHitCountsByEventIdSet(eventIds);
        log.info("EventService: getAllPrivate.");

        return events.stream()
                .map((Event event) -> eventMapper.toShortDto(event,
                        confirmedRequests.get(event.getId()),
                        hitCounts.get(event.getId()))
                )
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FullEventDto updatePrivate(long userId, UpdateEventRequest eventDto) {
        eventDateValidation(eventDto.getEventDate());
        Event event = getByIdAndThrow(eventDto.getEventId());
        if (userId != event.getInitiator().getId()) {
            throw new ValidationException("Event can be edited only by creator.");
        }
        if (event.getState() == State.PUBLISHED) {
            throw new ValidationException("Only rejected or pending events can be edited.");
        }
        if (event.getState() == State.CANCELED) {
            event.setState(State.PENDING);
        }
        eventUpdatePreparation(eventMapper.fromUpdateEventRequest(eventDto), event);
        Map<Long, Integer> hitCounts = getHitCountsByEventIdSet(Set.of(event.getId()));
        log.info("EventService: updatePrivate. Event {}.", event);

        return eventMapper.toFullDto(event,
                requestService.getConfirmed(event.getId()),
                hitCounts.get(event.getId()));
    }

    @Override
    @Transactional
    public FullEventDto createPrivate(long userId, NewEventDto newEventDto) {
        eventDateValidation(newEventDto.getEventDate());
        Event event = eventMapper.fromNewDto(newEventDto);
        event.setCategory(CategoryMapper.fromCategoryDto(categoryService.getById(newEventDto.getCategory())));
        event.setInitiator(userService.getById(userId));
        repository.save(event);
        Map<Long, Integer> hitCounts = getHitCountsByEventIdSet(Set.of(event.getId()));
        log.info("EventService: createPrivate. Event {}.", event);

        return eventMapper.toFullDto(event,
                requestService.getConfirmed(event.getId()),
                hitCounts.get(event.getId()));
    }

    @Override
    public FullEventDto getByIdPrivate(long userId, long eventId) {
        Event event = repository.findByInitiatorIdAndId(userId, eventId);
        if (event == null) {
            throw new NotFoundException("Event {} not found.", eventId);
        }
        Map<Long, Integer> hitCounts = getHitCountsByEventIdSet(Set.of(eventId));
        log.info("EventService: getByIdPrivate. EventId {}.", eventId);

        return eventMapper.toFullDto(event,
                requestService.getConfirmed(eventId),
                hitCounts.get(event.getId()));
    }

    @Override
    @Transactional
    public FullEventDto cancelPrivate(long userId, long eventId) {
        Event event = getByIdAndThrow(eventId);
        if (userId != event.getInitiator().getId()) {
            throw new ValidationException("Event can be canceled only by creator.");
        }
        if (event.getState().equals(State.CANCELED) || event.getState().equals(State.PUBLISHED)) {
            throw new ValidationException("Only pending event can be canceled.");
        }
        event.setState(State.CANCELED);
        Map<Long, Integer> hitCounts = getHitCountsByEventIdSet(Set.of(eventId));
        log.info("EventService: cancelPrivate. EventId {}.", eventId);

        return eventMapper.toFullDto(event,
                requestService.getConfirmed(eventId),
                hitCounts.get(event.getId()));
    }

    @Override
    public List<RequestDto> getEventRequestsPrivate(long userId, long eventId) {
        Event event = getByIdAndThrow(eventId);
        if (userId != event.getInitiator().getId()) {
            throw new ValidationException("Only creator can retrieve requests on the event.");
        }
        List<RequestDto> requests = requestService.getAllByEventId(eventId);
        log.info("EventService: getEventRequestsPrivate.");

        return requests;
    }

    @Override
    @Transactional
    public RequestDto confirmEventRequestPrivate(long userId, long eventId, long requestId) {
        Event event = getByIdAndThrow(eventId);
        Request request = requestService.getById(requestId);
        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            return RequestMapper.toRequestDto(request);
        }
        Long confirmedRequests = requestService.getConfirmed(eventId);
        if (confirmedRequests.equals((long) event.getParticipantLimit())) {
            log.info("Limit of requests for the event is reached.");
            throw new ValidationException("Limit of requests for the event is reached.");
        }
        request.setStatus(Status.CONFIRMED);

        requestService.save(request);
        log.info("EventService: confirmEventRequestPrivate. RequestId {}.", requestId);

        return RequestMapper.toRequestDto(request);
    }

    @Override
    @Transactional
    public RequestDto rejectEventRequestPrivate(long userId, long eventId, long requestId) {
        getByIdAndThrow(eventId);
        Request request = requestService.getById(requestId);
        if (request.getStatus() == Status.REJECTED || request.getStatus() == Status.CANCELED) {
            throw new ValidationException("Request has already canceled or rejected.");
        }
        request.setStatus(Status.REJECTED);
        requestService.save(request);
        log.info("EventService: rejectEventRequestPrivate. RequestId {}.", requestId);

        return RequestMapper.toRequestDto(request);
    }

    @Override
    public Event getById(long eventId) {
        log.info("EventService: getById. EventId {}.", eventId);

        return getByIdAndThrow(eventId);
    }

    @Override
    public List<ShortEventDto> getAllPublic(String text,
                                            Long[] categories,
                                            Boolean paid,
                                            LocalDateTime rangeStart,
                                            LocalDateTime rangeEnd,
                                            Boolean onlyAvailable,
                                            String sortType,
                                            Integer from,
                                            Integer size,
                                            HttpServletRequest request) {
        if (categories != null) {
            for (Long categoryId : categories) {
                categoryService.getById(categoryId);
            }
        }

        Sort sort = "EVENT_DATE".equals(sortType) ?
                Sort.sort(Event.class).by(Event::getEventDate).ascending() :
                Sort.unsorted();
        if (onlyAvailable == null) {
            onlyAvailable = true;
        }
        if (paid == null) {
            paid = true;
        }
        List<LocalDateTime> ranges = eventDatePreparation(rangeStart, rangeEnd);

        List<Event> events = repository.findAllByParam(
                text,
                categories,
                paid,
                ranges.get(0),
                ranges.get(1),
                onlyAvailable,
                getPageable(from / size, size, sort));
        Set<Long> eventIds = events.stream().map(eventMapper::toId).collect(Collectors.toSet());
        Map<Long, Integer> hitCounts = getHitCountsByEventIdSet(eventIds);
        Map<Long, Long> confirmedRequests = requestService.getCountConfirmedByEventsId(eventIds);
        log.info("EventService: getAllPublic.");

        List<ShortEventDto> results = events.stream()
                .map((Event event) ->
                        eventMapper.toShortDto(event,
                                confirmedRequests.get(event.getId()),
                                hitCounts.get(event.getId()))
                )
                .collect(Collectors.toList());
        if (sortType.equals("EVENT_DATE")) {
            return results;
        } else {
            return results.stream().sorted((e1, e2) -> Integer.compare(e2.getViews(), e1.getViews()))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public FullEventDto getByIdPublic(long eventId) {
        Event event = repository.findByIdAndStateLike(eventId, State.PUBLISHED);
        if (event == null) {
            throw new NotFoundException("Event {} doesn't exist.", eventId);
        }
        Map<Long, Integer> hitCounts = getHitCountsByEventIdSet(Set.of(eventId));
        log.info("EventService: getByIdPublic. EventId {}.", eventId);

        return eventMapper.toFullDto(event,
                requestService.getConfirmed(eventId),
                hitCounts.get(event.getId())
        );
    }

    @Override
    public Set<Event> getAllByEvents(Set<Long> events) {
        log.info("EventService: getAllByEvents.");

        return repository.findAllByEvents(events);
    }

    @Override
    public Map<Long, Integer> getHitCountsByEventIdSet(Set<Long> eventIds) {
        Map<Long, Integer> results = new HashMap<>();
        if (eventIds != null && !eventIds.isEmpty()) {
            String[] uris = eventIds.stream()
                    .map(id -> "/events/" + id).toArray(String[]::new);
            List<ViewStatsDto> views = eventClient.getHits(
                            LocalDateTime.of(2023, 1, 1, 0, 0, 0),
                            LocalDateTime.now(),
                            uris,
                            false
                    )
                    .getBody();
            if (views != null && !views.isEmpty()) {
                views.forEach(view -> {
                    String[] parts = view.getUri().split("/");
                    Long eventId = Long.valueOf(parts[parts.length - 1]);
                    results.put(eventId, view.getHits());
                });
            }
            eventIds.forEach(eventId -> results.putIfAbsent(eventId, 0));
        }
        log.info("EventService: getHitCountsByEventIdSet.");
        return results;
    }

    private Event getByIdAndThrow(long eventId) {
        return repository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Event {} doesn't exist.", eventId));
    }

    private void eventDateValidation(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Event start time can't be earlier than now plus 2 hours.");
        }
    }

    private Pageable getPageable(int from, int size, Sort sort) {
        return new PageableRequest(from, size, sort);
    }

    private void eventUpdatePreparation(Event eventDto, Event event) {
        if (eventDto.getAnnotation() != null && !eventDto.getAnnotation().isBlank()) {
            event.setAnnotation(eventDto.getAnnotation());
        }
        if (eventDto.getCategory() != null) {
            event.setCategory(eventDto.getCategory());
        }
        if (eventDto.getDescription() != null && !eventDto.getDescription().isBlank()) {
            event.setDescription(eventDto.getDescription());
        }
        if (eventDto.getEventDate() != null) {
            event.setEventDate(eventDto.getEventDate());
        }
        if (eventDto.getPaid() != null) {
            event.setPaid(eventDto.getPaid());
        }
        if (eventDto.getParticipantLimit() != null) {
            event.setParticipantLimit(eventDto.getParticipantLimit());
        }
        if (eventDto.getTitle() != null && !eventDto.getTitle().isBlank()) {
            event.setTitle(eventDto.getTitle());
        }
    }

    private List<LocalDateTime> eventDatePreparation(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        LocalDateTime startDate = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        LocalDateTime endDate = LocalDateTime.parse("5000-01-01 00:00:00",
                DateTimeFormatter.ofPattern(DATE_TIME_STRING));
        if (rangeStart != null) {
            startDate = rangeStart;
        }
        if (rangeEnd != null) {
            endDate = rangeEnd;
        }
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Start can't be afterEnd.");
        }

        return List.of(startDate, endDate);
    }

}
