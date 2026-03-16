package com.mybook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MyBookApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyBookApplication.class, args);
        System.out.println("""
                 __  __       ____              _
                |  \\/  |_   _| __ )  ___   ___ | | __
                | |\\/| | | | |  _ \\ / _ \\ / _ \\| |/ /
                | |  | | |_| | |_) | (_) | (_) |   <
                |_|  |_|\\__, |____/ \\___/ \\___/|_|\\_\\
                        |___/          
                """);
    }
}