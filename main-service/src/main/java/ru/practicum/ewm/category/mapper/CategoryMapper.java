package ru.practicum.ewm.category.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.CategoryInfo;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.model.Category;

@Mapper
public interface CategoryMapper {
    Category toEntity(NewCategoryDto newCategoryDto);

    CategoryDto toDto(Category category);

    CategoryDto toDto(CategoryInfo projection);
}
