package org.vrajpatel.personalexpense.Mapper;

import org.mapstruct.*;
import org.vrajpatel.personalexpense.model.PersonalExpenseModel;
import org.vrajpatel.personalexpense.requestDto.PatchExpenseDTO;
import org.vrajpatel.personalexpense.responseDto.PersonalExpenseDto;

@Mapper(componentModel = "spring")
public interface ExpensePatchMapper {


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "receipt", ignore = true)   // add this too
    @Mapping(target = "updatedAt", ignore = true)
    void updateExpenseFromDto(PatchExpenseDTO dto, @MappingTarget PersonalExpenseModel expense);

    @Mapping(source = "category.categoryId", target = "categoryId")
    @Mapping(source = "category.categoryName", target = "categoryName")
    @Mapping(source = "category.categoryColor", target = "categoryColor")
    @Mapping(source= "receipt.receiptId", target = "receiptId")
    @Mapping(source="receipt.receiptFileUrl", target = "receiptUrl")
    PersonalExpenseDto mapDTOToExpense(PersonalExpenseModel expense);

}
