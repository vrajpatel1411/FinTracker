package org.vrajpatel.userauthservice.requestDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Generic JWT wrapper. Used by POST /validate (pass an access token) and POST /getNewAccessToken (pass a refresh token).")
@Data
@NoArgsConstructor
public class JwtDto {

    @Schema(
            description = "A signed JWT. For POST /validate, supply the access token. For POST /getNewAccessToken, supply the refresh token.",
            example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI1NTBlODQwMC4uLiJ9.signature",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @JsonProperty("token")
    private String jwt;

    public String getJwt() {
        return jwt;
    }
    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public JwtDto(String jwt) {
        this.jwt = jwt;
    }
}
