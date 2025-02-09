package org.vrajpatel.userauthservice.ResponseDTO;

import lombok.Data;

@Data
public class AuthResponse {

    private Boolean status;

    private String message;

    private String jwtToken;

    public AuthResponse(Boolean status, String message) {
        this.status = status;
        this.message = message;
    }

    public AuthResponse(String jwtToken, String message, Boolean status) {
        this.jwtToken = jwtToken;
        this.message = message;
        this.status = status;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public AuthResponse() {
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
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
}
