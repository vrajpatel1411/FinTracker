package org.vrajpatel.userauthservice.utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class SetCookies {

    @Value("${domain}")
    private String domain;

    public String getAccessCookie(String accessToken) {
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

    public String getRefreshToken(String refreshToken) {
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
    public String clearAccessCookie() {
        return ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .maxAge(0)
                .sameSite("None")
                .domain(domain)
                .secure(true)
                .path("/")
                .build().toString();
    }

    public String clearRefreshCookie() {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .maxAge(0)
                .sameSite("None")
                .domain(domain)
                .secure(true)
                .path("/")
                .build().toString();
    }

    public HttpHeaders setCookies(HttpHeaders headers, String accessToken, String refreshToken){
        headers.add("Set-Cookie", getAccessCookie(accessToken));
        headers.add("Set-Cookie", getRefreshToken(refreshToken));
        return headers;
    }
}
