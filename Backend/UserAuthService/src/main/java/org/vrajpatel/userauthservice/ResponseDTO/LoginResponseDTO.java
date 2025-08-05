package org.vrajpatel.userauthservice.ResponseDTO;

import lombok.Data;

@Data
public class LoginResponseDTO {

    private String accessToken;

    private String refreshToken;


    private boolean isEmailVerified;

    private String email;

    public LoginResponseDTO(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public LoginResponseDTO() {

    }
}
