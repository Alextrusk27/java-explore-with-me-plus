package ru.practicum.ewm.category.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.sharing.constants.ApiPaths;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.Admin.CATEGORIES)
public class AdminCategoryController {
    private final CategoryService service;

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public CategoryDto addCategory(@RequestBody @Valid NewCategoryDto newCategoryDto) {
        return service.addCategory(newCategoryDto);
    }

    @PatchMapping("/{catId}")
    public CategoryDto updateCategory(@PathVariable Long catId, @RequestBody @Valid CategoryDto categoryDto) {
        return service.updateCategory(catId, categoryDto);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        service.deleteCategory(catId);
    }
}
