package org.vrajpatel.userauthservice.utils.Oauth2UserInfo;

import java.util.Map;

public class FacebookUserInfo extends UserInfo {


    public FacebookUserInfo(Map<String, Object> userInfo) {super(userInfo);}
    @Override
    public String getName() {
        return this.userInfo.get("name").toString();
    }


    @Override
    public String getEmail() {
        return this.userInfo.get("email").toString();
    }

    @Override
    public String getId() {
        return this.userInfo.get("id").toString();
    }

    @Override
    public String getPhotoUrl() {
        if(this.userInfo.containsKey("picture")) {
            Object picture = this.userInfo.get("picture");
            if(picture instanceof Map<?, ?>) {
                Map<?, ?> pictureMap = (Map<?, ?>) picture;
                Object dataObj = pictureMap.get("data");
                if(dataObj instanceof Map<?, ?>) {
                    Map<?, ?> dataMap = (Map<?, ?>) dataObj;
                    Object urlObj = dataMap.get("url");
                    if(urlObj instanceof String) {
                        return (String) urlObj;
                    }
                }
            }
        }
        return null;
    }
}
