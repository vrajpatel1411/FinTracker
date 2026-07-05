package org.vrajpatel.userauthservice.ResponseDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "Response returned when a new access token is issued from a refresh token")
@Data
public class AccessTokenResponse {

    @Schema(description = "Newly issued JWT access token (valid for 5 minutes)", example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI1NTBl...")
    private String accessToken;

    @Schema(description = "Email address of the authenticated user", example = "jane.doe@example.com")
    private String userEmail;

    @Schema(description = "UUID of the authenticated user", example = "550e8400-e29b-41d4-a716-446655440000")
    private String userId;

}
