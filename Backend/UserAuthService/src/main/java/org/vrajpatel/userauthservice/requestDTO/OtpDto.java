package org.vrajpatel.userauthservice.requestDTO;

import lombok.Data;

@Data
public class OtpDto {

    private String otp;

    private String userEmail;
}
