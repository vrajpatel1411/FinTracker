package org.vrajpatel.personalexpense.responseDto;

import lombok.Data;

@Data
public class CategoryDTO {

    private String categoryColor;
    private String categoryName;
    private String categoryId;

    public CategoryDTO() {

    }

    public CategoryDTO(String categoryColor, String categoryName, String categoryId) {
        this.categoryColor = categoryColor;
        this.categoryName = categoryName;
        this.categoryId = categoryId;
    }
}
