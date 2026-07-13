package com.mybook.mybook.user.biz;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.mybook.mybook.user.biz.domain.mapper")
@EnableFeignClients(basePackages = "com.mybook.mybook")
public class MyBookUserBizApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyBookUserBizApplication.class, args);
    }
}
