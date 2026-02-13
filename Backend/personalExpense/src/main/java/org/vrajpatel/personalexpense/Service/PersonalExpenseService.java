package org.vrajpatel.personalexpense.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jdk.jfr.Category;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.vrajpatel.personalexpense.Exception.Types.AddExpenseException;
import org.vrajpatel.personalexpense.Exception.Types.CategoryNotFoundError;
import org.vrajpatel.personalexpense.Exception.Types.UserNotFoundError;
import org.vrajpatel.personalexpense.Mapper.ExpensePatchMapper;
import org.vrajpatel.personalexpense.Repository.CategoryRepository;
import org.vrajpatel.personalexpense.Repository.PersonalExpenseRepository;
import org.vrajpatel.personalexpense.Repository.UserRepository;
import org.vrajpatel.personalexpense.model.CategoriesModel;
import org.vrajpatel.personalexpense.model.PersonalExpenseModel;
import org.vrajpatel.personalexpense.model.User;
import org.vrajpatel.personalexpense.requestDto.PatchExpenseDTO;
import org.vrajpatel.personalexpense.requestDto.AddExpenseDto;
import org.vrajpatel.personalexpense.responseDto.PersonalExpenseDto;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class PersonalExpenseService {

    private final Logger log = Logger.getLogger(PersonalExpenseService.class.getName());

    private final CategoryRepository categoryRepository;

    private final UserRepository userRepository;

    private final PersonalExpenseRepository personalExpenseRepository;

    private final ExpensePatchMapper expenseMapper;

    PersonalExpenseService(CategoryRepository categoryRepository, @Qualifier("expensePatchMapperImpl") ExpensePatchMapper expenseMapper, UserRepository userRepository, PersonalExpenseRepository personalExpenseRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.personalExpenseRepository = personalExpenseRepository;
        this.expenseMapper=expenseMapper;
    }

    @Cacheable(value="personalExpenses",key="{#page,#size}")
    @Transactional
    public Page<PersonalExpenseDto> findAll(String userId,int page, int size) {
        log.info("Finding all personal expenses");
        Sort sortAsc = Sort.by("expenseDate").descending().and(Sort.by("updatedAt").descending());
        Pageable pageable = PageRequest.of(page, size, sortAsc);
        return personalExpenseRepository.findAllByUserId(UUID.fromString(userId), pageable);
    }



    @Transactional
    @CacheEvict(value = "personalExpenses", allEntries = true)
    public PersonalExpenseDto addExpense(String userEmail, String userId, AddExpenseDto expense) throws AddExpenseException {
        try {
            User user = userRepository.findById(UUID.fromString(userId)).orElseThrow(() -> new UserNotFoundError("User Not Found with Id " + userId));
            PersonalExpenseModel newExpense = new PersonalExpenseModel();
            CategoriesModel category = categoryRepository
                    .findById(UUID.fromString(expense.getCategoryId()))
                    .orElseThrow(() -> new CategoryNotFoundError("Category Specified is Wrong or Has issue "));
            newExpense.setTitle(expense.getTitle());
            newExpense.setDescription(expense.getDescription());
            newExpense.setAmount(expense.getAmount());
            newExpense.setExpenseDate(expense.getExpenseDate());
            newExpense.setCategory(category);
            newExpense.setUser(user);
            if (expense.isReceipt()) {
            }
            PersonalExpenseModel addedExpense=personalExpenseRepository.save(newExpense);
            log.info("Added expense " + addedExpense);
            return expenseMapper.mapDTOToExpense(addedExpense);
        }
        catch(Exception e){
            throw new AddExpenseException("Faced Exception adding the expense " + e.getMessage());
        }



    }

    @Transactional
    @CacheEvict(value = "personalExpenses", allEntries = true)
    public PersonalExpenseDto updateExpense(String expenseId, PatchExpenseDTO expense) {
        PersonalExpenseModel personalExpense=personalExpenseRepository.findById(UUID.fromString(expenseId)).orElseThrow(()->new EntityNotFoundException("Expense Not found"));
        expenseMapper.updateExpenseFromDto(expense,personalExpense);
        personalExpense.setUpdatedAt(new Date());
        if(expense.getCategoryId()!=null && expense.getCategoryId()!=UUID.fromString(expenseId)) {
            Optional<CategoriesModel> category=categoryRepository.findById(expense.getCategoryId());
            if(category.isPresent()) {
                personalExpense.setCategory(category.get());
            }
        }
        log.info("Updated expense " + personalExpense.toString());
        PersonalExpenseModel updatedExpense=personalExpenseRepository.save(personalExpense);
        return expenseMapper.mapDTOToExpense(updatedExpense);
    }

    @Transactional
    @CacheEvict(value = "personalExpenses", allEntries = true)
    public Boolean deleteExpense(String expenseId) {
        PersonalExpenseModel expense=personalExpenseRepository.findById(UUID.fromString(expenseId)).orElseThrow(()->new EntityNotFoundException("Expense Not found"));
        expense.setDeleted(true);
        personalExpenseRepository.save(expense);
        return true;
    }

}
