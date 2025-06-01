package org.vrajpatel.userauthservice.utils.Oauth2Handler;

import jakarta.persistence.Column;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;

import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.vrajpatel.userauthservice.Exception.BadRequestException;
import org.vrajpatel.userauthservice.utils.CookiesUtil;
import org.vrajpatel.userauthservice.utils.HttpCookieOauth2;
import org.vrajpatel.userauthservice.utils.JwtUtils.TokenProvider;
import org.vrajpatel.userauthservice.utils.config.AppProperties;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import static org.vrajpatel.userauthservice.utils.HttpCookieOauth2.REDIRECT_URI_PARAM_COOKIE_NAME;

@Component
public class Oauth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private AppProperties appProperties;


    @Autowired
    private HttpCookieOauth2 cookieOauth2;



    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String targetUrl= determineTargetUrl(request, response,authentication);

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        String token=null;
        if(!targetUrl.isEmpty()){
            token= tokenProvider.createJWT(authentication);
        }

        if(token!=null){
            ResponseCookie cookie=ResponseCookie.from("jwttoken",token) .httpOnly(true)
                    .maxAge(3600)
                    .sameSite("None")
                    .domain("localhost")
                    .secure(true)
                    .path("/")
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE,cookie.toString());
        }

        clearAuthenticationAttributes(request);
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(targetUrl)
                .queryParam("success", "true");
        targetUrl = builder.toUriString();
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
