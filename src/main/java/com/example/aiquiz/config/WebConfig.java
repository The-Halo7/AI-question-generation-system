package com.example.aiquiz.config;

import com.example.aiquiz.interceptor.TokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TokenInterceptor())
                .addPathPatterns("/api/**")  // 拦截所有api请求
                .excludePathPatterns(
                    "/api/login",  // 登录接口不拦截
                    "/api/upload/sets",  // 获取题目集列表不拦截
                    "/api/upload/sets/*"  // 获取具体题目集不拦截
                );
    }
} 