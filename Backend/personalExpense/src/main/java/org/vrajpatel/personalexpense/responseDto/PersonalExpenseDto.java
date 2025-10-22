package org.vrajpatel.personalexpense.responseDto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.vrajpatel.personalexpense.model.CategoriesModel;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Data
public class PersonalExpenseDto implements Serializable {


    private UUID expenseId;

    private String title;

    private String description;

    private BigDecimal amount;

    private Date expenseDate;

    private UUID categoryId;

    private String categoryName;

    private String categoryColor;

    private UUID receiptId;

    private String receiptUrl;
}
