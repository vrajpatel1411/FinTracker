package org.vrajpatel.userauthservice.utils.Oauth2Handler;

import jakarta.persistence.Column;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;

import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.vrajpatel.userauthservice.Exception.BadRequestException;
import org.vrajpatel.userauthservice.service.AuthService;
import org.vrajpatel.userauthservice.utils.CookiesUtil;
import org.vrajpatel.userauthservice.utils.HttpCookieOauth2;
import org.vrajpatel.userauthservice.utils.JwtUtils.TokenProvider;
import org.vrajpatel.userauthservice.utils.OTPService.EmailService;
import org.vrajpatel.userauthservice.utils.OTPService.OTP;
import org.vrajpatel.userauthservice.utils.UserPrincipal;
import org.vrajpatel.userauthservice.utils.config.AppProperties;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.vrajpatel.userauthservice.utils.HttpCookieOauth2.REDIRECT_URI_PARAM_COOKIE_NAME;

@Component
public class Oauth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private AppProperties appProperties;


    @Autowired
    private HttpCookieOauth2 cookieOauth2;

    @Autowired
    private OTP otpService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${domain}")
    private String domain;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String targetUrl= determineTargetUrl(request, response,authentication);

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        if(userPrincipal.isEmailVerified()){
            String accessToken=null;
            String refreshToken=null;
            if(!targetUrl.isEmpty()){
                accessToken= tokenProvider.createJWT('A',(UserPrincipal) authentication.getPrincipal());
                refreshToken=tokenProvider.createJWT('R',(UserPrincipal) authentication.getPrincipal());
            }

            if(accessToken!=null && refreshToken!=null){
                ResponseCookie cookie=ResponseCookie.from("accessToken",accessToken) .httpOnly(true)
                    .maxAge(3600)
                    .sameSite("None")
                    .domain(domain)
                    .secure(true)
                    .path("/")
                    .build();

                ResponseCookie cookie2=ResponseCookie.from("refreshToken",refreshToken) .httpOnly(true)
                    .maxAge(3600)
                    .sameSite("None")
                    .domain(domain)
                    .secure(true)
                    .path("/")
                    .build();
                response.addHeader(HttpHeaders.SET_COOKIE,cookie.toString());
                response.addHeader(HttpHeaders.SET_COOKIE,cookie2.toString());
            }
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString(targetUrl)
                    .queryParam("status", true);
            targetUrl = builder.toUriString();
        }
        else {
            String otp= otpService.generateOTP();
            emailService.sendEmail(userPrincipal.getEmail(),"OTP Verification","Your OTP is: "+otp);
            stringRedisTemplate.opsForValue().set("OTP:"+userPrincipal.getEmail().toLowerCase(), otp, 120, TimeUnit.SECONDS);
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString(targetUrl)
                    .queryParam("email", userPrincipal.getEmail())
                    .queryParam("status", false);
            targetUrl = builder.toUriString();
        }

        clearAuthenticationAttributes(request);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);

    }
    protected final void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        cookieOauth2.removeAuthorizationRequest(request, response);
    }
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,  Authentication authentication) {
        Optional<String> redirectUri= CookiesUtil.getCookie(request,REDIRECT_URI_PARAM_COOKIE_NAME).map(Cookie::getValue);

        if(redirectUri.isPresent() && !isAuthorizedRedirectURI(redirectUri.get())){
            throw new BadRequestException("Sorry, We've got an unauthorized redirect URI");
        }
        String targetUrl=redirectUri.orElse(getDefaultTargetUrl());
        return UriComponentsBuilder.fromUriString(targetUrl).build().toUriString();
    }

    private boolean isAuthorizedRedirectURI(String uri){
        URI clientRedirectUri=URI.create(uri);

        return appProperties
                .getOauth2()
                .getAuthorizedRedirectUris()
                .stream()
                .anyMatch(
                        isAuthorizedRedirectURI->
                        {
                            URI authorizedUri=URI.create(isAuthorizedRedirectURI);
                            if(authorizedUri.getHost().equalsIgnoreCase(clientRedirectUri.getHost())  && authorizedUri.getPort()== clientRedirectUri.getPort()){

                                return true;
                            }
                            return false;
                        }
                );
    }
}
