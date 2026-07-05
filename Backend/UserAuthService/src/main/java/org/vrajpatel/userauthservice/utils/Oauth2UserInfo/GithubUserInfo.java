package org.vrajpatel.userauthservice.utils.Oauth2UserInfo;

import java.util.Map;

public class GithubUserInfo extends UserInfo{

        public GithubUserInfo(Map<String, Object> userInfo) {super(userInfo);}
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
            return this.userInfo.get("avatar_url").toString();
        }

}
