package org.vrajpatel.userauthservice.requestDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.aspectj.bridge.Message;

@Data
public class UserDTO {

    @Email(message = "Email is not in proper format")
    @NotNull(message = "Email should not be empty")
    private String email;

    @NotNull(message = "Password cannot be empty")
    private String password;

    public UserDTO(String username, String password) {
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
