package com.example.aiquiz.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(
                        "http://localhost:3000",     // 开发环境
                        "http://localhost:8080",     // 本地测试
                        "http://121.43.181.190:3000",  // 生产环境（添加端口号）
                        "http://121.43.181.190:8080"   // 生产环境API
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)  // 改为 false，因为我们不需要发送 cookie
                .maxAge(3600);
    }
}