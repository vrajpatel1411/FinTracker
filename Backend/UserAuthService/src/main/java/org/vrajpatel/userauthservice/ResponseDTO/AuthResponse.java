package org.vrajpatel.userauthservice.ResponseDTO;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class AuthResponse {

    private Boolean status;

    private String message;


    private String OTP;

    @Email
    private String email;


    private boolean needEmailVerification;


    public AuthResponse(Boolean status, String message) {
        this.status = status;
        this.message = message;
    }

    public AuthResponse( String message, Boolean status) {

        this.message = message;
        this.status = status;
    }



    public AuthResponse() {
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
