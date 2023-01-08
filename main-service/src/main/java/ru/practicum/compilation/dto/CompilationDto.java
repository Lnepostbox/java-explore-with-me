package ru.practicum.compilation.dto;

import lombok.*;
import ru.practicum.event.dto.ShortEventDto;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CompilationDto {
    private List<ShortEventDto> events;
    private Long id;
    private boolean pinned;
    private String title;
}
