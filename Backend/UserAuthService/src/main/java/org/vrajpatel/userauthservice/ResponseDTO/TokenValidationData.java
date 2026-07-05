package org.vrajpatel.userauthservice.ResponseDTO;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Identity claims extracted from a validated access token.
 * Returned by {@code POST /userauth/api/auth/validate} — consumed by the API Gateway
 * to identify the caller on every downstream request.
 */
@Schema(description = "Identity claims extracted from a validated access token — returned by POST /auth/validate")
public class TokenValidationData {

    @Schema(description = "UUID of the authenticated user", example = "550e8400-e29b-41d4-a716-446655440000")
    private final String userId;

    @Schema(description = "Email address of the authenticated user", example = "jane.doe@example.com")
    private final String userEmail;

    public TokenValidationData(String userId, String userEmail) {
        this.userId    = userId;
        this.userEmail = userEmail;
    }

    public String getUserId()    { return userId; }
    public String getUserEmail() { return userEmail; }
}
