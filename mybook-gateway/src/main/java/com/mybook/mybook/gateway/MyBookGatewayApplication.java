package com.mybook.mybook.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration.class
})
public class MyBookGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyBookGatewayApplication.class, args);
    }
}
