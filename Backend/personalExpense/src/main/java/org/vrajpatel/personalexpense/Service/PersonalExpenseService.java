package org.vrajpatel.personalexpense.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.vrajpatel.personalexpense.Repository.CategoryRepository;
import org.vrajpatel.personalexpense.Repository.PersonalExpenseRepository;
import org.vrajpatel.personalexpense.model.CategoriesModel;
import org.vrajpatel.personalexpense.model.PersonalExpenseModel;
import org.vrajpatel.personalexpense.responseDto.PersonalExpenseDto;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonalExpenseService {

    @Autowired
    private final CategoryRepository categoryRepository;

    @Autowired
    private final PersonalExpenseRepository personalExpenseRepository;

    public Page<PersonalExpenseDto> findAll(int page, int size) {

        Sort sortAsc = Sort.by("expenseDate").descending();
        Pageable pageable = PageRequest.of(page, size,sortAsc);
        Page<PersonalExpenseModel> data=personalExpenseRepository.findAll(pageable);

        Page<PersonalExpenseDto> dtoPage=null;

        if(data.hasContent()){
            dtoPage=data.map(p -> {

                CategoriesModel category=p.getCategory();

                PersonalExpenseDto personalExpenseDto=new PersonalExpenseDto();
                personalExpenseDto.setExpenseId(p.getExpenseId());
                personalExpenseDto.setCategoryId(category.getCategoryId());
                personalExpenseDto.setCategoryName(category.getCategoryName());
                personalExpenseDto.setAmount(p.getAmount());
                personalExpenseDto.setTitle(p.getTitle());
                Date expenseDate=p.getExpenseDate();

                personalExpenseDto.setExpenseDate(expenseDate);
                personalExpenseDto.setDescription(p.getDescription());
                personalExpenseDto.setCategoryColor(category.getCategoryColor());

                if(p.getReceipt() != null){
                    personalExpenseDto.setReceiptId(p.getReceipt().getReceiptId());
                    personalExpenseDto.setReceiptUrl(p.getReceipt().getReceiptFileUrl());
                }
                return personalExpenseDto;
            });
        }
        return dtoPage;
    }

}
