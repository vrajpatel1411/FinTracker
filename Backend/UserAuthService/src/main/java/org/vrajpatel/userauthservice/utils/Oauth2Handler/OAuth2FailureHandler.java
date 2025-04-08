package org.vrajpatel.userauthservice.utils.Oauth2Handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.vrajpatel.userauthservice.utils.CookiesUtil;
import org.vrajpatel.userauthservice.utils.HttpCookieOauth2;

import java.io.IOException;

import static org.vrajpatel.userauthservice.utils.HttpCookieOauth2.REDIRECT_URI_PARAM_COOKIE_NAME;

@Component
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Autowired
    private HttpCookieOauth2 httpCookieOauth2;


    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String targetUrl = CookiesUtil.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue)
                .orElse(("/"));

        targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("success", "false")
                .build().toUriString();

        httpCookieOauth2.removeAuthorizationRequest(request, response);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}