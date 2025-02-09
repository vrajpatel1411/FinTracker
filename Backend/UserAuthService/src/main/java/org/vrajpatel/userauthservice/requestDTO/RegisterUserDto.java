package org.vrajpatel.userauthservice.requestDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterUserDto {

    @Email
    @NotNull(message = "Email not provided")
    private String Email;

    @NotNull(message = "Please provide password")
    private String Password;

    @NotNull(message = "firstname is compulsory")
    private String firstName;

    private String lastName;

    public RegisterUserDto() {
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }
}
