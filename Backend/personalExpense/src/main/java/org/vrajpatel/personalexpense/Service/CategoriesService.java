package org.vrajpatel.personalexpense.Service;

import org.springframework.stereotype.Service;
import org.vrajpatel.personalexpense.Mapper.CategoryMapper;
import org.vrajpatel.personalexpense.Repository.CategoryRepository;
import org.vrajpatel.personalexpense.responseDto.CategoryDTO;

import java.util.List;
import java.util.UUID;

@Service
public class CategoriesService {

    private final CategoryRepository categoryRepository;

    private final CategoryMapper categoryMapper;
    CategoriesService(CategoryRepository CategoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = CategoryRepository;
        this.categoryMapper = categoryMapper;
    }


    public List<CategoryDTO> getCategories(String userId) {
        return categoryMapper.mapCategoryDTO(categoryRepository.findCategoriesModelByUserId(UUID.fromString(userId)));
    }
}
