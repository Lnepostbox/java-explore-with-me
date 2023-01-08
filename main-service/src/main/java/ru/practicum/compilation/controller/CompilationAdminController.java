package ru.practicum.compilation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.service.CompilationService;
import javax.validation.Valid;

@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
@Validated
@Slf4j
public class CompilationAdminController {

    private final CompilationService compilationService;

    @PostMapping
    public CompilationDto create(@Valid @RequestBody NewCompilationDto newCompilationDto) {
        log.info("CompilationAdminController: create.");

        return compilationService.create(newCompilationDto);
    }

    @DeleteMapping("/{compId}")
    public void delete(@PathVariable long compId) {
        log.info("CompilationAdminController: delete.");

        compilationService.delete(compId);
    }

    @DeleteMapping("/{compId}/events/{eventId}")
    public void deleteEvent(@PathVariable long compId, @PathVariable long eventId) {
        log.info("CompilationAdminController: deleteEvent.");

        compilationService.deleteEvent(compId, eventId);
    }

    @PatchMapping("/{compId}/events/{eventId}")
    public void createEvent(@PathVariable long compId, @PathVariable long eventId) {
        log.info("CompilationAdminController: createEvent.");

        compilationService.createEvent(compId, eventId);
    }

    @PatchMapping("/{compId}/pin")
    public void pin(@PathVariable long compId) {
        log.info("CompilationAdminController: pin.");

        compilationService.pin(compId);
    }

    @DeleteMapping("/{compId}/pin")
    public void unpin(@PathVariable long compId) {
        log.info("CompilationAdminController: unpin.");

        compilationService.unpin(compId);
    }
}