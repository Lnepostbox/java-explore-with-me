package ru.practicum.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.service.CategoryService;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/categories")
@Validated
@RequiredArgsConstructor
@Slf4j
public class CategoryPublicController {
    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryDto> getAll(@PositiveOrZero @RequestParam(value = "from", defaultValue = "0") int from,
                                    @Positive @RequestParam(value = "size", defaultValue = "10") int size) {
        log.info("CategoryPublicController: getAll.");

        return categoryService.getAll(from, size);
    }

    @GetMapping("/{id}")
    public CategoryDto getById(@PathVariable("id") long categoryId) {
        log.info("CategoryPublicController: getById.");

        return categoryService.getById(categoryId);
    }
}
