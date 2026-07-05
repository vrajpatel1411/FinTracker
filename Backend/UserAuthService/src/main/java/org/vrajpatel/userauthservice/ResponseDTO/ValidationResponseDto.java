package org.vrajpatel.userauthservice.ResponseDTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response returned by token validation endpoints")
public class ValidationResponseDto {

    @Schema(description = "True if the token is valid (or was successfully refreshed)", example = "true")
    private boolean valid;

    @Schema(description = "Human-readable result message", example = "Token is valid")
    private String message;

    @Schema(description = "Email address extracted from the validated token — populated by POST /validate only", example = "jane.doe@example.com")
    private String userEmail;

    @Schema(description = "UUID of the authenticated user — populated by POST /validate only", example = "550e8400-e29b-41d4-a716-446655440000")
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserEmail() {
        return userEmail;
    }

    @Override
    public String toString() {
        return "ValidationResponseDto{" +
                "message='" + message + '\'' +
                ", valid=" + valid +
                ", userEmail='" + userEmail + '\'' +
                '}';
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
