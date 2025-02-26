package org.vrajpatel.userauthservice.requestDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class JwtDto {

    @JsonProperty("jwt")
    private String jwt;

    public String getJwt() {
        return jwt;
    }
    public void setJwt(String jwt) {
        this.jwt = jwt;
    }
    JwtDto(String jwt) {
        this.jwt = jwt;
    }
}
