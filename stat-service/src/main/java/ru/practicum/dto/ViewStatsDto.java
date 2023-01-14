package ru.practicum.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ViewStatsDto {
    private String app;
    private String uri;
    private int hits;
}
