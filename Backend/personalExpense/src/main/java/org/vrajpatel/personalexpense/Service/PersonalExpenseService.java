package org.vrajpatel.personalexpense.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.vrajpatel.personalexpense.Repository.CategoryRepository;
import org.vrajpatel.personalexpense.model.CategoriesModel;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonalExpenseService {

    private final CategoryRepository categoryRepository;

    public List<String> findAll() {

        List<CategoriesModel> categories = categoryRepository.findAll();

        List<String> result = categories.stream()
                .map(c -> c.getCategoryName().toUpperCase() + " - " + c.getCategoryDescription().toUpperCase())
                .toList();

        return result;
    }

}
