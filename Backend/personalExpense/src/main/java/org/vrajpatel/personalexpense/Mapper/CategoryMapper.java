package org.vrajpatel.personalexpense.Mapper;

import org.mapstruct.*;
import org.vrajpatel.personalexpense.model.CategoriesModel;
import org.vrajpatel.personalexpense.responseDto.CategoryDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCategoryFromDto(CategoryDTO dto, @MappingTarget CategoriesModel categories) ;

    @Mapping(source = "categoryId", target = "categoryId")
    @Mapping(source = "categoryName", target = "categoryName")
    @Mapping(source = "categoryColor", target = "categoryColor")
    List<CategoryDTO> mapCategoryDTO(List<CategoriesModel> categories);
}
