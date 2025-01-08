package com.example.aiquiz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.aiquiz.mapper")
public class AiQuizApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiQuizApplication.class, args);
    }
} 