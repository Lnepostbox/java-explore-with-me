package ru.practicum.service;

import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.model.StatViewForSpecific;
import java.util.List;
import java.util.Map;

public interface EndpointHitService {

    void create(EndpointHitDto endpointHitDto);

    List<ViewStatsDto> getStats(StatViewForSpecific criteria);

    Map<String, Integer> getHitByUriList(List<String> uris);
}
