package org.vrajpatel.userauthservice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash
@Data
public class UserTokens {

    private String accessToken;

    private String refreshToken;

    private User user;
}
