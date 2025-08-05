package org.vrajpatel.userauthservice.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.vrajpatel.userauthservice.utils.Oauth2UserInfo.UserInfo;

@Component
public class HttpCookieOauth2 implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME="oauth2_auth_request";
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME="redirect_uri";

    private static final  int cookieMaxAge=3600;


    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return CookiesUtil.getCookie(request,OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
                .map(cookie -> CookiesUtil.deserialize(cookie,OAuth2AuthorizationRequest.class)).orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        if(authorizationRequest != null) {
            CookiesUtil.setCookie(response,OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,CookiesUtil.serialize(authorizationRequest),cookieMaxAge);
            String redirectionUri =request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
            if(redirectionUri != null) {
                CookiesUtil.setCookie(response,REDIRECT_URI_PARAM_COOKIE_NAME,redirectionUri,cookieMaxAge);
            }
            return;
        }
        CookiesUtil.removeCookie(request,response,OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        CookiesUtil.removeCookie(request,response,REDIRECT_URI_PARAM_COOKIE_NAME);
    }



    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        return this.loadAuthorizationRequest(request);
    }

    public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        CookiesUtil.removeCookie(request,response,OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        CookiesUtil.removeCookie(request,response,REDIRECT_URI_PARAM_COOKIE_NAME);
    }
}
