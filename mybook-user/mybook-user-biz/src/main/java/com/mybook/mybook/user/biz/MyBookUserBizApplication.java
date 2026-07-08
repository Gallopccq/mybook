package com.mybook.mybook.user.biz;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.mybook.mybook.user.biz.domain.mapper")
public class MyBookUserBizApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyBookUserBizApplication.class, args);
    }
}
