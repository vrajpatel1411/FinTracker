package org.vrajpatel.userauthservice.ResponseDTO;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Payload returned when login or registration succeeds but the user's email
 * address has not yet been verified. The client should redirect to the OTP
 * entry screen and pre-fill the email field with {@code email}.
 */
@Schema(description = "Returned when a login or registration attempt requires email OTP verification before access is granted")
public class EmailVerificationData {

    @Schema(description = "Email address that the OTP was sent to — use this to pre-fill the OTP screen", example = "jane.doe@example.com")
    private final String email;

    @Schema(description = "Always true — signals the client to redirect to the OTP entry screen", example = "true")
    private final boolean needEmailVerification = true;

    public EmailVerificationData(String email) {
        this.email = email;
    }

    public String  getEmail()                { return email; }
    public boolean isNeedEmailVerification() { return needEmailVerification; }
}
