package org.vrajpatel.userauthservice.requestDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JwtDto {

    @JsonProperty("jwt")
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
