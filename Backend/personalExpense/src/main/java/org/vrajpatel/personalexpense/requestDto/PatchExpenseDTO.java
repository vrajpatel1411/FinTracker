package org.vrajpatel.personalexpense.requestDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@Data
public class PatchExpenseDTO {
    private String title;

    private String description;

    private BigDecimal amount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expenseDate;
//    private Date expenseDate;

    private UUID categoryId;

    private UUID receiptId;

    private String receiptUrl;
}
