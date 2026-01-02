package ru.practicum.ewm.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.CreateCategoryDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
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
    public CategoryDto addCategory(CreateCategoryDto createCategoryDto) {
        if (repository.existsByName(createCategoryDto.name())) {
            throw new ConflictException("Категория с таким именем " + createCategoryDto.name() + " уже существует");
        }
        return categoryMapper.toDto(repository.save(categoryMapper.toEntity(createCategoryDto)));
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        if (repository.existsByNameAndIdNot(categoryDto.name(), id)) {
            throw new ConflictException(
                    "Категория с таким именем " + categoryDto.name() + " уже существует"
            );
        }

        Category category = getCategory(id);
        category.setName(categoryDto.name());
        return categoryMapper.toDto(repository.save(category));
    }


    @Override
    public CategoryDto getCategoryById(Long id) {
        return categoryMapper.toDto(getCategory(id));
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
                .map(categoryMapper::toDto).collect(Collectors.toList());
    }
}
