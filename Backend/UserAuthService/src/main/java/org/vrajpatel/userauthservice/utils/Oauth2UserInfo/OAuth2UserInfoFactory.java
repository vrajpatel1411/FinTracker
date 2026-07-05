package org.vrajpatel.userauthservice.utils.Oauth2UserInfo;

import org.vrajpatel.userauthservice.model.AuthProvider;

import java.util.Map;

public class OAuth2UserInfoFactory {


    public static UserInfo getUserInfo(String provider,  Map<String, Object> userInfo) {

        if(provider.equalsIgnoreCase(AuthProvider.facebook.toString())){
            return new FacebookUserInfo(userInfo);
        }else if(provider.equalsIgnoreCase(AuthProvider.github.toString())){
            return new GithubUserInfo(userInfo);
        }
        else if(provider.equalsIgnoreCase(AuthProvider.google.toString())){
            return new GoogleUserInfo(userInfo);
        }
        else{
            throw new IllegalArgumentException("provider not supported: " + provider);
        }
    }
}
