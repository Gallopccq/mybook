package com.mybook.mybook.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@MapperScan("com.mybook.mybook.auth.domain.mapper")
@EnableDiscoveryClient
public class MyBookAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyBookAuthApplication.class, args);
    }
}