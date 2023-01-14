package ru.practicum.request.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.request.model.Request;

@UtilityClass
public class RequestMapper {
    public RequestDto toRequestDto(Request request) {
        return RequestDto.builder()
                .id(request.getId())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .created(request.getCreated())
                .status(request.getStatus().toString())
                .build();
    }
}
