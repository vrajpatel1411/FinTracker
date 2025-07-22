package org.vrajpatel.fintrackergateway.ResponseDto;

import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;

import java.util.UUID;

public class ValidationResponseDto {

    private boolean valid;
    private String message;
    private String userEmail;

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
