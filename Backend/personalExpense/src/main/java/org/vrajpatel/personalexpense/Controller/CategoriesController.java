package org.vrajpatel.personalexpense.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.vrajpatel.personalexpense.Exception.Types.UnAuthorizedException;
import org.vrajpatel.personalexpense.Service.CategoriesService;
import org.vrajpatel.personalexpense.model.CategoriesModel;
import org.vrajpatel.personalexpense.responseDto.CategoryDTO;
import org.vrajpatel.personalexpense.responseDto.GenericResponseDTO;

import java.util.List;


@RestController
@RequestMapping("/personal/api")
public class CategoriesController {

    private static final Logger logger = LoggerFactory.getLogger(CategoriesController.class);

    private final CategoriesService categoriesService;


    CategoriesController(CategoriesService categoriesService) {
        this.categoriesService = categoriesService;
    }

    @GetMapping("/category/")
    public ResponseEntity<GenericResponseDTO<?>> getCategories(@RequestHeader("userEmail") String userEmail,
                                                              @RequestHeader("userId") String userId) throws UnAuthorizedException, Exception {
        if(userEmail.isEmpty() || userId.isEmpty() ) {
            throw new UnAuthorizedException("Unauthorized User");
        }

        try {
            List<CategoryDTO> categories= categoriesService.getCategories(userId);

            if(categories.isEmpty()) {
                GenericResponseDTO<String> genericResponseDTO = GenericResponseDTO.error("No categories found");
                genericResponseDTO.setError("True");
                return new ResponseEntity<>(genericResponseDTO, HttpStatus.NOT_FOUND);
            }
//            logger.info("categories :"+categories.toString());
            return ResponseEntity.ok(GenericResponseDTO.success(categories));
        }
        catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
