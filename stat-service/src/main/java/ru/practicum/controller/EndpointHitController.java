package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.model.StatViewForSpecific;
import ru.practicum.service.EndpointHitService;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class EndpointHitController {

    private final EndpointHitService endpointHitService;

    @PostMapping("/hit")
    public void create(@RequestBody EndpointHitDto endpointHitDto) {
        endpointHitService.create(endpointHitDto);
        log.info("EndpointHitController: create.");
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(StatViewForSpecific criteria) {
        log.info("EndpointHitController: getStats.");

        return endpointHitService.getStats(criteria);
    }
}
