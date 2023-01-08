package ru.practicum.category.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.service.CategoryService;
import javax.validation.Valid;

@RestController
@RequestMapping("/admin/categories")
@AllArgsConstructor
@Slf4j
public class CategoryAdminController {
    private final CategoryService categoryService;

    @PostMapping
    public CategoryDto create(@Valid @RequestBody NewCategoryDto newCategoryDto) {
        log.info("CategoryAdminController: create.");

        return categoryService.create(newCategoryDto);
    }

    @PatchMapping
    public CategoryDto update(@Valid @RequestBody CategoryDto categoryDto) {
        log.info("CategoryAdminController: update.");

        return categoryService.update(categoryDto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") long categoryId) {
        log.info("CategoryAdminController: delete.");

        categoryService.delete(categoryId);
    }
}

