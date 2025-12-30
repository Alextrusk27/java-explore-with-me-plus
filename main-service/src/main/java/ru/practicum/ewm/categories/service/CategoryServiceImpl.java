package ru.practicum.ewm.categories.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.categories.dto.CategoryDto;
import ru.practicum.ewm.categories.dto.NewCategoryDto;
import ru.practicum.ewm.categories.mapper.CategoryMapper;
import ru.practicum.ewm.categories.model.Category;
import ru.practicum.ewm.categories.repository.CategoryRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository repository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        if (repository.existsByName(newCategoryDto.name())) {
            throw new ConflictException("Категория с таким именем " + newCategoryDto.name() + " уже существует");
        }
        return categoryMapper.toCategoryDto(repository.save(categoryMapper.toCategory(newCategoryDto)));
    }

    @Override
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        Category category = getCategory(id);
        category.setName(categoryDto.name());
        return categoryMapper.toCategoryDto(repository.save(category));
    }

    @Override
    public CategoryDto getCategoryById(Long id) {
        return categoryMapper.toCategoryDto(getCategory(id));
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Category with id = " + id + " was not found");
        }
        repository.deleteById(id);
    }

    @Override
    public Category getCategory(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new NotFoundException("Category with id = " + id + " was not found"));
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        return repository.findAll(PageRequest.of(from / size, size)).stream()
                .map(categoryMapper::toCategoryDto).collect(Collectors.toList());
    }
}
