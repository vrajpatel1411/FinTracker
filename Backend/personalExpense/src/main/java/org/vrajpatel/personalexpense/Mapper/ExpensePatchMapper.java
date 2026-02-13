package org.vrajpatel.personalexpense.Mapper;

import org.mapstruct.*;
import org.vrajpatel.personalexpense.model.PersonalExpenseModel;
import org.vrajpatel.personalexpense.requestDto.PatchExpenseDTO;
import org.vrajpatel.personalexpense.responseDto.PersonalExpenseDto;

import java.beans.BeanProperty;

@Mapper(componentModel = "spring")
public interface ExpensePatchMapper {


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateExpenseFromDto(PatchExpenseDTO dto, @MappingTarget PersonalExpenseModel expense);

    @Mapping(source = "category.categoryId", target = "categoryId")
    @Mapping(source = "category.categoryName", target = "categoryName")
    @Mapping(source = "category.categoryColor", target = "categoryColor")
    PersonalExpenseDto mapDTOToExpense(PersonalExpenseModel expense);

}
