package org.vrajpatel.userauthservice.ResponseDTO;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Newly issued access token and the associated user identity.
 * Returned by {@code POST /userauth/api/auth/getNewAccessToken} — intended for
 * non-browser clients (mobile apps, server-to-server) that cannot use cookies.
 */
@Schema(description = "Newly issued access token and associated user identity — returned by POST /auth/getNewAccessToken")
public class AccessTokenData {

    @Schema(description = "Newly issued JWT access token (valid for 5 minutes)", example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI1NTBl...")
    private final String accessToken;

    @Schema(description = "UUID of the authenticated user", example = "550e8400-e29b-41d4-a716-446655440000")
    private final String userId;

    @Schema(description = "Email address of the authenticated user", example = "jane.doe@example.com")
    private final String userEmail;

    public AccessTokenData(String accessToken, String userId, String userEmail) {
        this.accessToken = accessToken;
        this.userId      = userId;
        this.userEmail   = userEmail;
    }

    public String getAccessToken() { return accessToken; }
    public String getUserId()      { return userId; }
    public String getUserEmail()   { return userEmail; }
}
