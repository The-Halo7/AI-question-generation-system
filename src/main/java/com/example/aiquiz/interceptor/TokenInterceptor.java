package com.example.aiquiz.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.util.StringUtils;

public class TokenInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果是OPTIONS请求，放行
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        
        // 从请求头中获取token
        String token = request.getHeader("Authorization");
        if (!StringUtils.hasText(token)) {
            response.setStatus(401);
            response.getWriter().write("未登录");
            return false;
        }
        
        // TODO: 这里可以添加token的验证逻辑
        // 目前简单实现，只要有token就通过
        return true;
    }
} 