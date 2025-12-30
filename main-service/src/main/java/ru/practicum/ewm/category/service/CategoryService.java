package ru.practicum.ewm.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.exception.ConflictException;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository repository;
    private final CategoryMapper categoryMapper;

    @Transactional
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        if (repository.existsByName(newCategoryDto.getName())) {
            throw new ConflictException("Категория с таким именем " + newCategoryDto.getName() + " уже существует");
        }
        Category category = categoryMapper.toEntity(newCategoryDto);
        category = repository.save(category);
        return categoryMapper.toDto(category);
    }
}