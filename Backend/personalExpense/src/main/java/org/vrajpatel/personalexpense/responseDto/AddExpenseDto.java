package org.vrajpatel.personalexpense.responseDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Data
public class AddExpenseDto {

    @NotNull
    private String title;

    @NotNull
    private String description;

    @NotNull
    private BigDecimal amount;

    @NotNull
    private Date expenseDate;

    @NotNull
    private String categoryId;

    @NotNull
    private boolean isReceipt;

    private MultipartFile receipt;

}
