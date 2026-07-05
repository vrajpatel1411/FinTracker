package org.vrajpatel.userauthservice.requestDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "Request payload for submitting an email OTP for verification")
@Data
public class OtpDto {

    @Schema(description = "6-digit one-time password received via email", example = "847291", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @NotBlank
    @Size(max=6, min=6, message = "Otp must be at 6 Digits")
    private String otp;

    @Schema(description = "Email address the OTP was sent to", example = "jane.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @NotBlank
    @Email(message = "Invalid email format")
    private String userEmail;
}
