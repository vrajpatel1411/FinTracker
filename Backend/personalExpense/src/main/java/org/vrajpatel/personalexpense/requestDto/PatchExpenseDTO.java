package org.vrajpatel.personalexpense.requestDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

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
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate expenseDate;

    private UUID categoryId;

    @JsonProperty("isReceipt")
    private boolean hasReceipt;

    @JsonProperty("deleteReceipt")
    private boolean deleteReceipt;

    private String fileName;

    private String fileType;

    private Integer fileLength;

}
