package org.vrajpatel.userauthservice.requestDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterUserDto {

    @Email
    @NotNull(message = "Email not provided")
    private String email;

    @NotNull(message = "Please provide password")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotNull(message = "firstname is compulsory")
    private String firstName;

    private String lastName;
}
