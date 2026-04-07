package org.vrajpatel.userauthservice.requestDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OtpDto {

    @NotNull
    @NotBlank
    private String otp;

    @NotNull
    @NotBlank
    @Email(message = "Invalid email format")
    private String userEmail;
}
