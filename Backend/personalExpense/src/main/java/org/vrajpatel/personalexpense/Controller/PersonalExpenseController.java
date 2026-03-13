package org.vrajpatel.personalexpense.Controller;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;

import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.vrajpatel.personalexpense.Exception.Types.AddExpenseException;
import org.vrajpatel.personalexpense.Exception.Types.UnAuthorizedException;
import org.vrajpatel.personalexpense.Service.PersonalExpenseService;

import org.vrajpatel.personalexpense.requestDto.PatchExpenseDTO;
import org.vrajpatel.personalexpense.requestDto.AddExpenseDto;
import org.vrajpatel.personalexpense.responseDto.GenericResponseDTO;
import org.vrajpatel.personalexpense.responseDto.PersonalExpenseDto;

@RestController
@RequestMapping("/personal/api")
public class PersonalExpenseController {

    public final Logger log=LoggerFactory.getLogger(PersonalExpenseController.class);


    private final PersonalExpenseService personalExpenseService;

    public PersonalExpenseController(PersonalExpenseService personalExpenseService){
        this.personalExpenseService=personalExpenseService;
    }

    @GetMapping("/")
    public ResponseEntity<GenericResponseDTO<PagedModel<EntityModel<PersonalExpenseDto>>>> getExpenses(
            @RequestHeader("userEmail") String userEmail,
            @RequestHeader("userId") String userId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            PagedResourcesAssembler<PersonalExpenseDto> assembler
    ) throws UnAuthorizedException {
        if(userEmail.isEmpty() || userId.isEmpty() ) {
            throw new UnAuthorizedException("Unauthorized User");
        }
        if (page < 0 || size <= 0 || size > 100) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(GenericResponseDTO.error("Invalid pagination parameters"));
        }
        try {
            Page<PersonalExpenseDto> data = personalExpenseService.findAll(userId,page, size);
            PagedModel<EntityModel<PersonalExpenseDto>> model = assembler.toModel(data);
            return ResponseEntity.ok(
                    GenericResponseDTO.success(model)
            );
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponseDTO.error("Internal Server Error: " + e.getMessage()));
        }
    }

    @PostMapping("/")
    public ResponseEntity<GenericResponseDTO<PersonalExpenseDto>> addExpense(@RequestHeader("userEmail") String userEmail,
                                                                 @RequestHeader("userId") String userId,@RequestBody AddExpenseDto expense) throws AddExpenseException, UnAuthorizedException {
        if(userEmail.isEmpty() || userId.isEmpty() ) {
             throw new UnAuthorizedException("Unauthorized User");
        }
        try{
            log.info("Adding expense {}", expense);
            PersonalExpenseDto expenseAdded = personalExpenseService.addExpense(userId,expense);
            if(expenseAdded==null) {
                throw new NullPointerException("Error Adding Expense");
            }
            return ResponseEntity.ok(GenericResponseDTO.success(expenseAdded));

        }catch(Exception e){
            throw new AddExpenseException("Error Adding expense : "+e.getMessage());
        }
    }

    @PatchMapping("/expense/{id}")
    public ResponseEntity<GenericResponseDTO<PersonalExpenseDto>> updateExpense(
            @PathVariable("id") String expenseId,
            @RequestHeader("userEmail") String userEmail,
            @RequestHeader("userId") String userId,
            @RequestBody PatchExpenseDTO expense) {

        if (userEmail.isBlank() || userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(GenericResponseDTO.error("Unauthorized User"));
        }
        if (expenseId.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(GenericResponseDTO.error("Expense ID is required"));
        }

        try {
            PersonalExpenseDto response = personalExpenseService.updateExpense(expenseId, userId, expense);
            return ResponseEntity.ok(GenericResponseDTO.success(response));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(GenericResponseDTO.error(e.getMessage()));
        } catch (UnAuthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(GenericResponseDTO.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponseDTO.error("Error updating expense: " + e.getMessage()));
        }
    }

    @DeleteMapping("/expense/{id}")
    public ResponseEntity<GenericResponseDTO<String>> deleteExpense(@PathVariable("id") String expenseId) throws Exception{
        if(expenseId.isEmpty()) {
            throw new NullPointerException("Expense Id is null or empty");
        }
        try {
            Boolean status = personalExpenseService.deleteExpense(expenseId);
            if(status) {
                GenericResponseDTO<String> response = GenericResponseDTO.success("Deleted expense");
                return ResponseEntity.ok(response);
            }
            else{
                GenericResponseDTO<String> response = GenericResponseDTO.error("Not Deleted");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        }
        catch(Exception e){
            throw new Exception(e.getMessage() + " Something went wrong.");
        }
    }
}
