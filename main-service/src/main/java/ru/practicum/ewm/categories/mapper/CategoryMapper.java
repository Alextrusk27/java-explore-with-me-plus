package ru.practicum.ewm.categories.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.practicum.ewm.categories.dto.CategoryDto;
import ru.practicum.ewm.categories.dto.NewCategoryDto;
import ru.practicum.ewm.categories.model.Category;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper {
    Category toCategory(NewCategoryDto newCategoryDto);

    Category toCategory(CategoryDto categoryDto);

    CategoryDto toCategoryDto(Category category);
}
