package org.vrajpatel.personalexpense.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.vrajpatel.personalexpense.Exception.CategoryNotFoundError;
import org.vrajpatel.personalexpense.Exception.UserNotFoundError;
import org.vrajpatel.personalexpense.Repository.CategoryRepository;
import org.vrajpatel.personalexpense.Repository.PersonalExpenseRepository;
import org.vrajpatel.personalexpense.Repository.UserRepository;
import org.vrajpatel.personalexpense.model.CategoriesModel;
import org.vrajpatel.personalexpense.model.PersonalExpenseModel;
import org.vrajpatel.personalexpense.model.User;
import org.vrajpatel.personalexpense.responseDto.AddExpenseDto;
import org.vrajpatel.personalexpense.responseDto.PersonalExpenseDto;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class PersonalExpenseService {

    private final Logger log = Logger.getLogger(PersonalExpenseService.class.getName());

    private final CategoryRepository categoryRepository;

    private final UserRepository userRepository;

    private final PersonalExpenseRepository personalExpenseRepository;

    PersonalExpenseService(CategoryRepository categoryRepository, UserRepository userRepository, PersonalExpenseRepository personalExpenseRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.personalExpenseRepository = personalExpenseRepository;
    }

    @Cacheable(value="personalExpenses",key="{#page,#size}")
    @Transactional
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

    @Transactional
    public boolean addExpense(String userEmail, String userId, AddExpenseDto expense) throws UserNotFoundError,CategoryNotFoundError {

        Optional<User> user=userRepository.findById(UUID.fromString(userId));

        if(user.isPresent()){
            PersonalExpenseModel newExpense=new PersonalExpenseModel();
            Optional<CategoriesModel> category = categoryRepository.findById(UUID.fromString(expense.getCategoryId()));

            if(category.isEmpty())
            {
                throw new CategoryNotFoundError("Category Specified is Wrong or Has issue ");
            }
            newExpense.setTitle(expense.getTitle());
            newExpense.setDescription(expense.getDescription());
            newExpense.setAmount(expense.getAmount());
            newExpense.setExpenseDate(expense.getExpenseDate());
            newExpense.setCategory(category.get());
            if(expense.isReceipt())
            {

            }

            personalExpenseRepository.save(newExpense);

            return true;

        }
        else{
            throw new UserNotFoundError("User Not Found with Id "+userId);
        }
    }
}
