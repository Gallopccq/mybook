package com.mybook.mybook.note.biz;

import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.mybook.mybook")
@MapperScan(basePackages = "com.mybook.mybook.note.biz.domain")
@Import(RocketMQAutoConfiguration.class)
public class MyBookNoteBizApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyBookNoteBizApplication.class,args);
    }
}
