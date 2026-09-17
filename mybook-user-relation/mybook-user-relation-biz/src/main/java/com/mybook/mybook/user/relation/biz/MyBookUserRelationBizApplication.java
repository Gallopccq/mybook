package com.mybook.mybook.user.relation.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.mybook.mybook")
@MapperScan(basePackages = "com.mybook.mybook.user.relation.biz.domain")
public class MyBookUserRelationBizApplication {
    public static void main(String[] args){
        SpringApplication.run(MyBookUserRelationBizApplication.class, args);
    }
}
