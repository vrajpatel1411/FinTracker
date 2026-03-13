package org.vrajpatel.userauthservice.utils.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private final Auth auth = new Auth();
    private final OAuth2 oauth2 = new OAuth2();

    public static class Auth {
        private String accessTokenSecret;
        private long accessTokenExpirationMsec;
        private long refreshTokenExpirationMsec;
        private String refreshTokenSecret;

        public String getRefreshTokenSecret() {
            return refreshTokenSecret;
        }

        public void setRefreshTokenSecret(String refreshTokenSecret) {
            this.refreshTokenSecret = refreshTokenSecret;
        }

        public long getRefreshTokenExpirationMsec() {
            return refreshTokenExpirationMsec;
        }

        public void setRefreshTokenExpirationMsec(long refreshTokenExpirationMsec) {
            this.refreshTokenExpirationMsec = refreshTokenExpirationMsec;
        }


        public String getAccessTokenSecret() {
            return accessTokenSecret;
        }

        public void setAccessTokenSecret(String tokenSecret) {
            this.accessTokenSecret = tokenSecret;
        }

        public long getAccessTokenExpirationMsec() {
            return accessTokenExpirationMsec;
        }

        public void setAccessTokenExpirationMsec(long tokenExpirationMsec) {
            this.accessTokenExpirationMsec = tokenExpirationMsec;
        }
    }

    public static final class OAuth2 {
        private List<String> authorizedRedirectUris = new ArrayList<>();

        public List<String> getAuthorizedRedirectUris() {
            return authorizedRedirectUris;
        }

        public OAuth2 authorizedRedirectUris(List<String> authorizedRedirectUris) {
            this.authorizedRedirectUris = authorizedRedirectUris;
            return this;
        }
    }

    public Auth getAuth() {
        return auth;
    }

    public OAuth2 getOauth2() {
        return oauth2;
    }
}