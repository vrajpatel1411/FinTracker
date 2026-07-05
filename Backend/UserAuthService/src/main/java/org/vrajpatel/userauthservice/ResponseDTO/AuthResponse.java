package org.vrajpatel.userauthservice.ResponseDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Schema(description = "Standard response returned by authentication endpoints")
@Data
public class AuthResponse {

    @Schema(description = "True if the operation succeeded, false if further action is required or an error occurred", example = "true")
    private Boolean status;

    @Schema(description = "Human-readable description of the result", example = "Successfully logged in")
    private String message;

    @Schema(hidden = true)
    private String OTP;

    @Schema(description = "Present when the user needs to complete email verification — contains the address the OTP was sent to", example = "jane.doe@example.com")
    @Email
    private String email;

    @Schema(description = "True when the client should redirect the user to the OTP entry screen", example = "true")
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
