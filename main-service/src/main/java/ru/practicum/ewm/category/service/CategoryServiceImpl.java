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
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryDto addCategory(CreateCategoryDto createCategoryDto) {
        if (categoryRepository.existsByName(createCategoryDto.name())) {
            throw new ConflictException("Категория с таким именем " + createCategoryDto.name() + " уже существует");
        }
        return categoryMapper.toDto(categoryRepository.save(categoryMapper.toEntity(createCategoryDto)));
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        if (categoryRepository.existsByNameAndIdNot(categoryDto.name(), id)) {
            throw new ConflictException(
                    "Категория с таким именем " + categoryDto.name() + " уже существует"
            );
        }

        Category category = getCategory(id);
        category.setName(categoryDto.name());
        return categoryMapper.toDto(categoryRepository.save(category));
    }


    @Override
    public CategoryDto getCategoryById(Long id) {
        return categoryMapper.toDto(getCategory(id));
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException("Category with id = " + id + " was not found");
        }
        if (eventRepository.existsByCategoryId(id)) {
            throw new ConflictException("The category is not empty");
        }
        categoryRepository.deleteById(id);
    }

    @Override
    public Category getCategory(Long id) {
        return categoryRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Category with id = " + id + " was not found"));
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        return categoryRepository.findAll(PageRequest.of(from / size, size)).stream()
                .map(categoryMapper::toDto).collect(Collectors.toList());
    }
}
