package org.vrajpatel.personalexpense.requestDto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Data
public class AddExpenseDto {

    @NotNull
    private String title;

    private String description;

    @NotNull
    private BigDecimal amount;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate expenseDate;

    @NotNull
    private String categoryId;

    @NotNull
    @JsonProperty("isReceipt")
    private boolean hasReceipt;

    private String fileName;

    private String fileType;

    private Integer fileLength;

}

//{"data":
// {
//  "expenseId":"3eb033e4-6336-4a00-8721-1642a1634d7f",
//  "title":"testing 2",
//  "description":"",
//  "amount":20,
//  "expenseDate":"2026-03-11",
//  "categoryId":"9db88be3-460c-4802-a0e6-57500444a1e4",
//  "categoryName":"Grocery",
//  "categoryColor":"#FF9800",
//  "receiptId":"db5da114-8d14-4b0b-93f6-d6dcbbe3b4f7",
//  "receiptUrl":"https://fintracker-receipts.s3.amazonaws.com/uploads/dedaf38b-d9ba-4641-b88a-dfc712d61b24/Citi.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20260311T045348Z&X-Amz-SignedHeaders=content-length%3Bcontent-type%3Bhost&X-Amz-Expires=300&X-Amz-Credential=AKIA6NRJRD7DUFPBB6KV%2F20260311%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Signature=32fd4fc6943de04cd12916f05c056fa2451dbc3e21743c599f4e79f084985c60"},
//  "status":"success"
//  }