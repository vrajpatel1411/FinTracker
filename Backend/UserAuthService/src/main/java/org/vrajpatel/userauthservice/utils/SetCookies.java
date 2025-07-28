package org.vrajpatel.userauthservice.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;


public class SetCookies {

    @Value("${domain}")
    private static String domain;

    public static String getAccessCookie(String accessToken) {
        ResponseCookie cookie=ResponseCookie.from("accessToken",accessToken)
                .httpOnly(true)
                .maxAge(300)
                .sameSite("None")
                .domain(domain)
                .secure(true)
                .path("/")
                .build();
        return cookie.toString();
    }

    public static String getRefreshToken(String refreshToken) {
        ResponseCookie cookie=ResponseCookie.from("refreshToken",refreshToken)
                .httpOnly(true)
                .maxAge(10*24*60*60)
                .sameSite("None")
                .domain(domain)
                .secure(true)
                .path("/")
                .build();
        return cookie.toString();
    }
    public static HttpHeaders setCookies(HttpHeaders headers, String accessToken, String refreshToken){

        headers.add("Set-Cookie",getAccessCookie(accessToken));
        headers.add("Set-Cookie",getRefreshToken(refreshToken));

        return headers;
    }
}
