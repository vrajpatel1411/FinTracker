package org.vrajpatel.userauthservice.requestDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "Request payload for registering a new user account")
@Data
public class RegisterUserDto {

    @Schema(description = "User's email address — used as the login identifier", example = "jane.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @Email
    @NotNull(message = "Email not provided")
    private String email;

    @Schema(description = "Account password — minimum 8 characters", example = "SecurePass123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Please provide password")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @Schema(description = "User's first name", example = "Jane", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "firstname is compulsory")
    private String firstName;

    @Schema(description = "User's last name (optional)", example = "Doe", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String lastName;
}
