package org.vrajpatel.userauthservice.ResponseDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import org.vrajpatel.userauthservice.model.User;

@Schema(description = "Response returned by the user profile endpoint")
public class UserResponse {

    @Schema(description = "The authenticated user's profile data")
    private User user;

    @Schema(description = "Human-readable result message", example = "User found")
    private String message;

    @Schema(description = "True if the user was found and returned successfully", example = "true")
    private Boolean status;


    public UserResponse() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
