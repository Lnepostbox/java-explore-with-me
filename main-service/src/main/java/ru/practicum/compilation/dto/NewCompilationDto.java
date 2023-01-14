package ru.practicum.compilation.dto;

import lombok.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class NewCompilationDto {
    private Set<Long> events;

    private boolean pinned;

    @NotBlank
    @Size(max = 512)
    private String title;
}
