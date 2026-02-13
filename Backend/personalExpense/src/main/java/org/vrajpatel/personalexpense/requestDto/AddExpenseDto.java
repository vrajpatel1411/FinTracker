package org.vrajpatel.personalexpense.requestDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class AddExpenseDto {

    @NotNull
    private String title;

    @NotNull
    private String description;

    @NotNull
    private BigDecimal amount;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd")
    private Date expenseDate;

    @NotNull
    private String categoryId;

    @NotNull
    private boolean isReceipt;

    private MultipartFile receipt;

}
