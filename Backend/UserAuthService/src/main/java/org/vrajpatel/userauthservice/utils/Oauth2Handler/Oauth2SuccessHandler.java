package org.vrajpatel.userauthservice.utils.Oauth2Handler;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.vrajpatel.userauthservice.Exception.BadRequestException;
import org.vrajpatel.userauthservice.service.AuthService;
import org.vrajpatel.userauthservice.utils.*;
import org.vrajpatel.userauthservice.utils.JwtUtils.TokenProvider;

import org.vrajpatel.userauthservice.utils.config.AppProperties;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import static org.vrajpatel.userauthservice.utils.HttpCookieOauth2.REDIRECT_URI_PARAM_COOKIE_NAME;

@Component
public class Oauth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenProvider tokenProvider;
    private final AppProperties appProperties;
    private final RefreshToken refreshTokenUtil;
    private final HttpCookieOauth2 cookieOauth2;
    private final SetCookies setCookies;
    private final AuthService authService;

    public Oauth2SuccessHandler(TokenProvider tokenProvider,
                                AppProperties appProperties,
                                RefreshToken refreshTokenUtil,
                                HttpCookieOauth2 cookieOauth2,
                                SetCookies setCookies, AuthService authService) {
        this.tokenProvider = tokenProvider;
        this.appProperties = appProperties;
        this.refreshTokenUtil = refreshTokenUtil;
        this.cookieOauth2 = cookieOauth2;
        this.setCookies = setCookies;
        this.authService = authService;
    }

    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest  request,@NonNull HttpServletResponse response,@NonNull Authentication authentication) throws IOException {
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        if (userPrincipal!=null && userPrincipal.isEmailVerified()) {
            String accessToken = tokenProvider.generateAccessToken(userPrincipal.getId(), userPrincipal.getEmail());
            String refreshToken = tokenProvider.generateRefreshToken(userPrincipal.getId(), userPrincipal.getEmail());
            response.addHeader(HttpHeaders.SET_COOKIE, setCookies.getAccessCookie(accessToken));
            response.addHeader(HttpHeaders.SET_COOKIE, setCookies.getRefreshToken(refreshToken));
            refreshTokenUtil.setRefreshToken(refreshToken, userPrincipal.getEmail());
            targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
                    .queryParam("status", true)
                    .toUriString();
        } else {
            if(userPrincipal==null) {
                throw new BadRequestException("Not able to authenticate");
            }
            try {
                authService.sendOTP(userPrincipal.getEmail());
            } catch (Exception e) {
                logger.error("Issue while Sending OTP : "+ e.getMessage());
                throw new BadRequestException("Issue while sending OTP to " + userPrincipal.getEmail());
            }
            targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
                    .queryParam("email", userPrincipal.getEmail())
                    .queryParam("status", false)
                    .toUriString();
        }
        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected final void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        cookieOauth2.removeAuthorizationRequest(request, response);
    }

    @NonNull
    protected String determineTargetUrl(@NonNull HttpServletRequest request,@NonNull HttpServletResponse response, Authentication authentication) {
        Optional<String> redirectUri = CookiesUtil.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME).map(Cookie::getValue);
        if (redirectUri.isPresent() && !isAuthorizedRedirectURI(redirectUri.get())) {
            throw new BadRequestException("Sorry, We've got an unauthorized redirect URI");
        }
        String targetUrl = redirectUri.orElse(getDefaultTargetUrl());
        return UriComponentsBuilder.fromUriString(targetUrl).build().toUriString();
    }

    private boolean isAuthorizedRedirectURI(String uri) {
        URI clientRedirectUri = URI.create(uri);
        return appProperties.getOauth2().getAuthorizedRedirectUris().stream()
                .anyMatch(authorizedRedirectUri -> {
                    URI authorizedUri = URI.create(authorizedRedirectUri);
                    return authorizedUri.getScheme().equalsIgnoreCase(clientRedirectUri.getScheme())
                            && authorizedUri.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
                            && authorizedUri.getPort() == clientRedirectUri.getPort();
                });
    }
}
