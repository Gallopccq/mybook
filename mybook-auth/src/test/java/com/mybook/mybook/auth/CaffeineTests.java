package com.mybook.mybook.auth;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

@SpringBootTest
@Slf4j
public class CaffeineTests {

    @Test
    void testBuild(){
        Cache<String, String> cache = Caffeine.newBuilder()
                .expireAfterWrite(60, TimeUnit.MILLISECONDS)
                .initialCapacity(100)
                .maximumSize(500)
                .recordStats()
                .build();
        cache.put("1", "hello");
        log.info(cache.getIfPresent("1"));
        log.info(String.valueOf(cache.stats()));
    }
}
