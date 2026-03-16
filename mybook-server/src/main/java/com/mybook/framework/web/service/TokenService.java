package com.mybook.framework.web.service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybook.common.constant.Constants;
import com.mybook.common.core.domain.model.LoginUser;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.nio.charset.StandardCharsets;

@Service
public class TokenService {
    private final StringRedisTemplate redis; // 这个是什么？Spring 框架封装的用于操作 Redis 的工具类
    private final ObjectMapper objectMapper;  // 这是什么？Java对象 和 JSON字符串 之间的转换器

    @Value("${token.secret}")
    private String secret;

    @Value("${token.expire-minutes}")
    private long expireMinutes;

    public TokenService(StringRedisTemplate redis, ObjectMapper objectMapper){
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    public String createToken(LoginUser loginUser){
        String token = UUID.randomUUID().toString().replace("-", "");
        loginUser.setToken(token);

        try {
            String json = objectMapper.writeValueAsString(loginUser);
            redis.opsForValue().set(Constants.LOGIN_TOKEN_KEY + token, json, Duration.ofMinutes(expireMinutes));
        } catch (Exception e) {
            throw new RuntimeException("写入Redis失败", e);
        }
        // 这个redis缓存是否与jwt的设计逻辑冲突，jwt用于无状态访问，而redis却保存了用户信息。

        Map<String, Object> claims = new HashMap<>();
        claims.put("token", token);

        return Jwts.builder()
                .setClaims(claims)
                .signWith(signingKey())
                .compact(); // compact 序列化Jwts为String
    }

    private Key signingKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        // HS256 要求 key >= 32 字节，不足时补齐
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Optional<LoginUser> parseLoginUser(String jwt) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(signingKey()).build().parseClaimsJws(jwt).getBody();
            String token = (String) claims.get("token");
            String json = redis.opsForValue().get(Constants.LOGIN_TOKEN_KEY + token);
            if (json == null) return Optional.empty();
            return Optional.of(objectMapper.readValue(json, LoginUser.class));
            // 这个Optional是干什么的？
            /**
             * Optional 类的引入很好的解决空指针异常。
             * Optional.of - 如果传递的参数是 null，抛出异常 NullPointerException
             * Optional.orElse - 如果值存在，返回它，否则返回默认值
             * Optional.get - 获取值，值需要存在
             */
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
