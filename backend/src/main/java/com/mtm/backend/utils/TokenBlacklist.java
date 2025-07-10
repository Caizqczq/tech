package com.mtm.backend.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class TokenBlacklist {
    private final StringRedisTemplate redisTemplate;

    private String key(String token){
        return "blacklist:" + token;
    }

    public void add(String token,long ttlMillis){
        if(token==null||ttlMillis<=0){
            return;
        }
        redisTemplate.opsForValue()
                .set(key(token),"",ttlMillis, TimeUnit.SECONDS);
    }

    public boolean contains(String token){
        if(token==null){
            return false;
        }
        return redisTemplate.hasKey(key(token));
    }
}
