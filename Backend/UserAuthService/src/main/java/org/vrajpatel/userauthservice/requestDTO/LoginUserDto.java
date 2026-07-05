package org.vrajpatel.userauthservice.requestDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Request payload for user login")
@Data
@NoArgsConstructor
public class LoginUserDto {

    @Schema(description = "Registered email address", example = "jane.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @Email(message = "Email is not in proper format")
    @NotNull(message = "Email should not be empty")
    private String email;

    @Schema(description = "Account password", example = "SecurePass123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Password cannot be empty")
    private String password;

    public LoginUserDto(String username, String password) {
        this.email = username;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
