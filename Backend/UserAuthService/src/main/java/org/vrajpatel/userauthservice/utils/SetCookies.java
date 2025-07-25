package org.vrajpatel.userauthservice.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;


public class SetCookies {

    @Value("${domain}")
    private static String domain;

    public static HttpHeaders setCookies(HttpHeaders headers, String accessToken, String refreshToken){
        ResponseCookie cookie=ResponseCookie.from("accessToken",accessToken)
                .httpOnly(true)
                .maxAge(300)
                .sameSite("None")
                .domain(domain)
                .secure(true)
                .path("/")
                .build();
        ResponseCookie cookie2=ResponseCookie.from("refreshToken",refreshToken)
                .httpOnly(true)
                .maxAge(10*24*60*60)
                .sameSite("None")
                .domain(domain)
                .secure(true)
                .path("/")
                .build();

        headers.add("Set-Cookie",cookie.toString());
        headers.add("Set-Cookie",cookie2.toString());

        return headers;
    }
}
