package ru.practicum.compilation.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.CompilationMapper;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.storage.CompilationRepository;
import ru.practicum.event.dto.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.service.EventService;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.service.RequestService;
import ru.practicum.util.PageableRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {
    private final EventService eventService;
    private final RequestService requestService;
    private final CompilationRepository compilationRepository;
    private final EventMapper eventMapper;
    private final CompilationMapper compilationMapper;

    @Override
    public List<CompilationDto> getAll(Boolean pinned, int from, int size) {
        List<Compilation> compilations = compilationRepository.findAllByPinnedIs(pinned,
                getPageable(from, size, Sort.unsorted()));
        Set<Long> eventIds = new HashSet<>();
        for (Compilation compilation : compilations) {
            Set<Long> subEventIds = compilation.getEvents().stream().map(eventMapper::toId).collect(Collectors.toSet());
            eventIds.addAll(subEventIds);
        }
        Map<Long, Long> confirmedRequests = requestService.getCountConfirmedByEventsId(eventIds);
        Map<Long, Integer>  hitCounts = eventService.getHitCountsByEventIdSet(eventIds);
        log.info("CompilationService: getAllAdmin.");

        return compilations.stream()
                .map((Compilation compilation) -> compilationMapper.toDto(compilation, confirmedRequests, hitCounts))
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getById(long compilationId) {
        Compilation compilation = getByIdAndThrow(compilationId);
        Set<Long> eventIds = compilation.getEvents().stream().map(eventMapper::toId).collect(Collectors.toSet());
        Map<Long, Long> confirmedRequests = requestService.getCountConfirmedByEventsId(eventIds);
        Map<Long, Integer>  hitCounts = eventService.getHitCountsByEventIdSet(eventIds);
        log.info("CompilationService: getById. CompilationId {}.", compilation);

        return compilationMapper.toDto(compilation, confirmedRequests, hitCounts);
    }

    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto newCompilationDto) {
        Compilation compilation = compilationMapper.fromNewCompilationDto(newCompilationDto);
        Set<Event> eventList = eventService.getAllByEvents(newCompilationDto.getEvents());
        compilation.setEvents(eventList);
        compilation = compilationRepository.save(compilation);
        Set<Long> eventIds = compilation.getEvents().stream().map(eventMapper::toId).collect(Collectors.toSet());
        Map<Long, Long> confirmedRequests = requestService.getCountConfirmedByEventsId(eventIds);
        Map<Long, Integer>  hitCounts = eventService.getHitCountsByEventIdSet(eventIds);
        log.info("CompilationService: create.");

        return compilationMapper.toDto(compilation, confirmedRequests, hitCounts);
    }

    @Override
    public void delete(long compilationId) {
        getByIdAndThrow(compilationId);
        compilationRepository.deleteById(compilationId);
        log.info("CompilationService: delete. CompilationId {}.", compilationId);
    }

    @Override
    @Transactional
    public void deleteEvent(long compilationId, long eventId) {
        Compilation compilation = getByIdAndThrow(compilationId);
        compilation.getEvents().remove(eventService.getById(eventId));
        log.info("CompilationService: deleteEvent. CompilationId {}, eventId {}", compilationId, eventId);
    }

    @Override
    @Transactional
    public void createEvent(long compilationId, long eventId) {
        Compilation compilation = getByIdAndThrow(compilationId);
        compilation.getEvents().add(eventService.getById(eventId));
        log.info("CompilationService: createEvent. CompilationId {}, eventId {}", compilationId, eventId);
    }

    @Override
    @Transactional
    public void pin(long compilationId) {
        Compilation compilation = getByIdAndThrow(compilationId);
        compilation.setPinned(true);
        log.info("CompilationService: pin.");
    }

    @Override
    @Transactional
    public void unpin(long compilationId) {
        Compilation compilation = getByIdAndThrow(compilationId);
        compilation.setPinned(false);
        log.info("CompilationService: unpin.");
    }

    private Compilation getByIdAndThrow(long compilationId) {
        return compilationRepository.findById(compilationId).orElseThrow(() ->
                new NotFoundException("Compilation {} doesn't exist", compilationId));
    }

    private Pageable getPageable(int from, int size, Sort sort) {
        return new PageableRequest(from, size, sort);
    }
}
