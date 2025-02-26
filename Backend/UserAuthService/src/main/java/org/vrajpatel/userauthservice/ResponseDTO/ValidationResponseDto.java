package org.vrajpatel.userauthservice.ResponseDTO;

public class ValidationResponseDto {

    public boolean valid;
    public String message;
    public String userEmail;

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
