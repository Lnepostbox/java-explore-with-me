package ru.practicum.request.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
public class RequestDto {
    private Long id;
    private Long event;
    private Long requester;
    private LocalDateTime created;
    private String status;
}
