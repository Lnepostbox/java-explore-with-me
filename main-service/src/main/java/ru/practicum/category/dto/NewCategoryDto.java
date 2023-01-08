package ru.practicum.category.dto;

import lombok.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class NewCategoryDto {
    @NotBlank
    @Size(max = 512)
    private String name;
}
