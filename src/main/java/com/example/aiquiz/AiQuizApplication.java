package com.example.aiquiz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class AiQuizApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiQuizApplication.class, args);
    }
} 