package com.mybook.mybook.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.mybook.mybook.auth.domain.mapper")
public class MyBookAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyBookAuthApplication.class, args);
    }
}