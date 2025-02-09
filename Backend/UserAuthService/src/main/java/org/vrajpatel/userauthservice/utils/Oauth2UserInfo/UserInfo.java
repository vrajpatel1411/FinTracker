package org.vrajpatel.userauthservice.utils.Oauth2UserInfo;

import java.util.Map;

public abstract class UserInfo {

    protected Map<String, Object> userInfo;

    public UserInfo(Map<String, Object> userInfo) {
        this.userInfo = userInfo;
    }

    public Map<String, Object> getUserInfo() {return userInfo;}

    public abstract String getName();

    public abstract String getEmail();

    public abstract String getPhotoUrl();

    public abstract String getId();
}
