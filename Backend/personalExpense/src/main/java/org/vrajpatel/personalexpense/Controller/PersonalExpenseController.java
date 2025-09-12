package org.vrajpatel.personalexpense.Controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.vrajpatel.personalexpense.Service.PersonalExpenseService;

import org.vrajpatel.personalexpense.responseDto.GenericResponseDTO;
import org.vrajpatel.personalexpense.responseDto.PersonalExpenseDto;

@RestController
@RequestMapping("/personal/api/")
public class PersonalExpenseController {

    public final Logger log=LoggerFactory.getLogger(PersonalExpenseController.class);

    @Autowired
    private PersonalExpenseService personalExpenseService;

    @GetMapping("/")
    public ResponseEntity<GenericResponseDTO<PagedModel<EntityModel<PersonalExpenseDto>>>> getExpenses(
            @RequestHeader("userEmail") String userEmail,
            @RequestHeader("userId") String userId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            PagedResourcesAssembler<PersonalExpenseDto> assembler
    ) {
        if (page < 0 || size <= 0 || size > 100) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(GenericResponseDTO.error("Invalid pagination parameters"));
        }
        try {
            Page<PersonalExpenseDto> data = personalExpenseService.findAll(page, size);
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
}
