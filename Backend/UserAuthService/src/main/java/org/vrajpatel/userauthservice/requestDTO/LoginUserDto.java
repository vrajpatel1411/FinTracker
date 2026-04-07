package org.vrajpatel.userauthservice.requestDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginUserDto {

    @Email(message = "Email is not in proper format")
    @NotNull(message = "Email should not be empty")
    private String email;

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
