package org.vrajpatel.userauthservice.requestDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "Request payload for resending an OTP to the user's email address")
@Data
public class ResendOtpDto {

    @Schema(description = "Email address to send the new OTP to", example = "jane.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Email
    private String email;
}
