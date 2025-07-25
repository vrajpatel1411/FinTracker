package org.vrajpatel.userauthservice.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.vrajpatel.userauthservice.model.User;

import java.util.concurrent.TimeUnit;


@Component
public class RefreshToken {

    private final StringRedisTemplate stringRedisTemplate;

    public RefreshToken(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void setRefreshToken(String refreshToken, String userEmail) {

        stringRedisTemplate.opsForValue().set("refresh_token : "+refreshToken, userEmail, 864000, TimeUnit.SECONDS);

        stringRedisTemplate.opsForSet().add("user_email : "+userEmail, refreshToken);

        stringRedisTemplate.expire("refresh_token : "+refreshToken, 864000, TimeUnit.SECONDS);

    }
}
