package ru.yandex.practicum.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.event.service.CategoryService;
import ru.yandex.practicum.interaction.dto.CategoryDto;
import ru.yandex.practicum.interaction.dto.NewCategoryDto;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryDto> add(@RequestBody @Valid NewCategoryDto dto) {
        log.info("Adding category: {}", dto);
        return ResponseEntity.status(201).body(categoryService.addCategory(dto));
    }

    @PatchMapping("/{catId}")
    public ResponseEntity<CategoryDto> update(@PathVariable Long catId,
                                              @RequestBody @Valid CategoryDto dto) {
        log.info("Updating category: {}", dto);
        return ResponseEntity.ok(categoryService.updateCategory(catId, dto));
    }

    @DeleteMapping("/{catId}")
    public ResponseEntity<Void> delete(@PathVariable Long catId) {
        log.info("Deleting category: {}", catId);
        categoryService.deleteCategory(catId);
        return ResponseEntity.noContent().build();
    }
}