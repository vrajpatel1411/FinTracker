package org.vrajpatel.personalexpense.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
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
import org.vrajpatel.personalexpense.Exception.Types.UnAuthorizedException;
import org.vrajpatel.personalexpense.Exception.Types.UserNotFoundError;
import org.vrajpatel.personalexpense.Mapper.ExpensePatchMapper;
import org.vrajpatel.personalexpense.Repository.CategoryRepository;
import org.vrajpatel.personalexpense.Repository.PersonalExpenseRepository;
import org.vrajpatel.personalexpense.Repository.ReceiptRepository;
import org.vrajpatel.personalexpense.Repository.UserRepository;
import org.vrajpatel.personalexpense.model.CategoriesModel;
import org.vrajpatel.personalexpense.model.PersonalExpenseModel;
import org.vrajpatel.personalexpense.model.ReceiptModel;
import org.vrajpatel.personalexpense.model.User;
import org.vrajpatel.personalexpense.requestDto.PatchExpenseDTO;
import org.vrajpatel.personalexpense.requestDto.AddExpenseDto;
import org.vrajpatel.personalexpense.responseDto.PersonalExpenseDto;
import org.vrajpatel.personalexpense.responseDto.PresignedUrlResponse;
import org.vrajpatel.personalexpense.utils.CacheEvictionService;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class PersonalExpenseService {

    private final CategoryRepository categoryRepository;

    private final UserRepository userRepository;

    private final PersonalExpenseRepository personalExpenseRepository;

    private final ExpensePatchMapper expenseMapper;

    private final S3Service s3Service;

    private final ReceiptRepository receiptRepository;

    private final CacheEvictionService cacheEvictionService;

    PersonalExpenseService(CacheEvictionService cacheEvictionService,ReceiptRepository receiptRepository,CategoryRepository categoryRepository, @Qualifier("expensePatchMapperImpl") ExpensePatchMapper expenseMapper, S3Service s3Service, UserRepository userRepository, PersonalExpenseRepository personalExpenseRepository) {
        this.categoryRepository = categoryRepository;
        this.receiptRepository=receiptRepository;
        this.cacheEvictionService = cacheEvictionService;
        this.s3Service = s3Service;
        this.userRepository = userRepository;
        this.personalExpenseRepository = personalExpenseRepository;
        this.expenseMapper=expenseMapper;
    }

    @Cacheable(value="personalExpenses",key="#userId+'_'+#page + '_' + #size")
    @Transactional
    public Page<PersonalExpenseDto> findAll(String userId,int page, int size) {
        Sort sortAsc = Sort.by("expenseDate").descending().and(Sort.by("updatedAt").descending());
        Pageable pageable = PageRequest.of(page, size, sortAsc);
        Page<PersonalExpenseDto> response=personalExpenseRepository.findAllByUserId(UUID.fromString(userId), pageable);
        response= response.map(expense->{
                        if(expense.getReceiptId()!=null) {
                            String presignedUrl=s3Service.generateGetPresignedUrl(expense.getReceiptUrl());
                            expense.setReceiptUrl(presignedUrl);
                        }
                        return expense;
                    }
                );

        return response;
    }

    @Transactional
    @CacheEvict(value = "analytics", key = "#userId")
    public PersonalExpenseDto addExpense(String userId, AddExpenseDto expense) throws AddExpenseException {
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
            PresignedUrlResponse response=null;
            if (expense.isHasReceipt()) {
                response= s3Service.generatePresignedUrl(expense.getFileName(), expense.getFileType(), expense.getFileLength());
               ReceiptModel receiptModel = new ReceiptModel();
               receiptModel.setReceiptFileUrl(response.getKey());
               receiptModel= receiptRepository.save(receiptModel);
               newExpense.setReceipt(receiptModel);
            }
            PersonalExpenseModel addedExpense=personalExpenseRepository.save(newExpense);
            PersonalExpenseDto dto = expenseMapper.mapDTOToExpense(addedExpense);
            if(expense.isHasReceipt() && response!=null){
                dto.setReceiptUrl(response.getUrl());
            }
            cacheEvictionService.evictCacheByPrefix("personalExpenses::"+userId);
            return dto;
        }
        catch(Exception e){
            throw new AddExpenseException("Faced Exception adding the expense " + e.getMessage());
        }
    }

    @Transactional
    @CacheEvict(value = "analytics", key = "#userId")
    public PersonalExpenseDto updateExpense(String expenseId,String userId, PatchExpenseDTO expense) throws UnAuthorizedException, CategoryNotFoundError {
        PersonalExpenseModel personalExpense=personalExpenseRepository
                                            .findById(UUID.fromString(expenseId))
                                            .orElseThrow(()->new EntityNotFoundException("Expense Not found"));
        if (!personalExpense.getUserId().equals(UUID.fromString(userId))) {
            throw new UnAuthorizedException("Expense does not belong to this user");
        }
        expenseMapper.updateExpenseFromDto(expense,personalExpense);
        if (expense.getCategoryId() != null) {
            CategoriesModel category = categoryRepository
                    .findById(expense.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundError("Category not found"));
            personalExpense.setCategory(category);
        }
        PresignedUrlResponse presignedResponse = null;
        if (expense.isDeleteReceipt() && personalExpense.getReceipt() != null) {
            s3Service.deleteObject(personalExpense.getReceipt().getReceiptFileUrl());
            ReceiptModel toDelete = personalExpense.getReceipt();
            personalExpense.setReceipt(null);
            personalExpenseRepository.save(personalExpense);
            receiptRepository.delete(toDelete);
        } else if (expense.isHasReceipt()) {
            presignedResponse = s3Service.generatePresignedUrl(
                    expense.getFileName(), expense.getFileType(), expense.getFileLength()
            );
            ReceiptModel receiptModel = personalExpense.getReceipt() != null
                    ? personalExpense.getReceipt()
                    : new ReceiptModel();
            receiptModel.setReceiptFileUrl(presignedResponse.getKey());
            receiptModel = receiptRepository.save(receiptModel);
            personalExpense.setReceipt(receiptModel);
        }
        personalExpense.setUpdatedAt(LocalDate.now());
        PersonalExpenseModel updatedExpense = personalExpenseRepository.save(personalExpense);
        PersonalExpenseDto dto = expenseMapper.mapDTOToExpense(updatedExpense);
        if (presignedResponse != null) {
            dto.setReceiptUrl(presignedResponse.getUrl()); // upload URL for frontend
        }
        cacheEvictionService.evictCacheByPrefix("personalExpenses::"+userId);
        return dto;
    }
    @Transactional
    @CacheEvict(value = "analytics", key = "#userId")
    public Boolean deleteExpense(String userId,String expenseId) {
        PersonalExpenseModel expense=personalExpenseRepository.findById(UUID.fromString(expenseId)).orElseThrow(()->new EntityNotFoundException("Expense Not found"));
        expense.setDeleted(true);
        personalExpenseRepository.save(expense);
        cacheEvictionService.evictCacheByPrefix("personalExpenses::"+userId);
        return true;
    }
}
