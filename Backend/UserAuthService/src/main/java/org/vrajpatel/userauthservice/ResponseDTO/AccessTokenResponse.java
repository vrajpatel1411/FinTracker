package org.vrajpatel.userauthservice.ResponseDTO;

import lombok.Data;

@Data
public class AccessTokenResponse {

    private String accessToken;

    private String userEmail;

    private String userId;

}
