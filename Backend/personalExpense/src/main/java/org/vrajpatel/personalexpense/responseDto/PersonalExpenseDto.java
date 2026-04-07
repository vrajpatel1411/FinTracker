package org.vrajpatel.personalexpense.responseDto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;
import org.vrajpatel.personalexpense.model.CategoriesModel;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@Data
public class PersonalExpenseDto implements Serializable {

    private UUID expenseId;

    private String title;

    private String description;

    private BigDecimal amount;

    private LocalDate expenseDate;


    private UUID categoryId;

    private String categoryName;

    private String categoryColor;

    private UUID receiptId;

    private String receiptUrl;
    public PersonalExpenseDto() {}

    public PersonalExpenseDto(UUID expenseId, String title, String description,BigDecimal amount,LocalDate expenseDate, UUID categoryId, String categoryName, String categoryColor, UUID receiptId, String receiptUrl) {
        this.amount = amount;
        this.categoryColor = categoryColor;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.description = description;
        this.expenseDate = expenseDate;
        this.expenseId = expenseId;
        this.receiptId = receiptId;
        this.receiptUrl = receiptUrl;
        this.title = title;
    }
}
