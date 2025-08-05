package org.vrajpatel.userauthservice.utils.OTPService;


import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class OTP {


    private final int len=6;

    private final SecureRandom secureRandom;

    OTP(){
        this.secureRandom=new SecureRandom();
    }

    public String generateOTP() {
        int OTPToken=100000+secureRandom.nextInt(900000);
        return String.valueOf(OTPToken);
    }


}
