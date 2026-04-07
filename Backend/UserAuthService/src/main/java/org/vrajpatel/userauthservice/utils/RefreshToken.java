package org.vrajpatel.userauthservice.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RefreshToken {

    private static final int MAX_DEVICES = 3;
    private static final long REFRESH_TOKEN_TTL_SECONDS = 864000;

    private final StringRedisTemplate stringRedisTemplate;

    public RefreshToken(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void setRefreshToken(String refreshToken, String userEmail) {
        String userKey = "user_email : " + userEmail;
        long now = System.currentTimeMillis();
        stringRedisTemplate.opsForValue().set("refresh_token : " + refreshToken, userEmail, REFRESH_TOKEN_TTL_SECONDS, TimeUnit.SECONDS);
        stringRedisTemplate.opsForZSet().add(userKey, refreshToken, now);
        stringRedisTemplate.expire(userKey, REFRESH_TOKEN_TTL_SECONDS, TimeUnit.SECONDS);
        Long size = stringRedisTemplate.opsForZSet().size(userKey);
        if (size != null && size > MAX_DEVICES) {
            Set<String> oldest = stringRedisTemplate.opsForZSet().range(userKey, 0, size - MAX_DEVICES - 1);
            if (oldest != null) {
                for (String oldToken : oldest) {
                    stringRedisTemplate.delete("refresh_token : " + oldToken);
                }
                stringRedisTemplate.opsForZSet().remove(userKey, oldest.toArray());
            }
        }
    }

    public void deleteRefreshToken(String refreshToken) {
        String email = stringRedisTemplate.opsForValue().get("refresh_token : " + refreshToken);
        stringRedisTemplate.delete("refresh_token : " + refreshToken);
        if (email != null) {
            stringRedisTemplate.opsForZSet().remove("user_email : " + email, refreshToken);
        }
    }
}
