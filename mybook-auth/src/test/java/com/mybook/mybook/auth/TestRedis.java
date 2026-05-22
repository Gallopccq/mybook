package com.mybook.mybook.auth;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
@Slf4j
public class TestRedis {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void testSetKeyValue(){
        redisTemplate.opsForValue().set("name","gallop");
    }

    @Test
    void testHasKey(){
        log.info("key 是否存在：{}", Boolean.TRUE.equals(redisTemplate.hasKey("name")));

    }

    @Test
    void testGetValue(){
        log.info("value 值：{}", redisTemplate.opsForValue().get("name"));
    }

    @Test
    void testDelete(){
        log.info(String.valueOf(redisTemplate.delete("name")));
    }
}
