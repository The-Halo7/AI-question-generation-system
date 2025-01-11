package com.example.aiquiz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.aiquiz.mapper.UserMapper;
import com.example.aiquiz.model.User;
import com.example.aiquiz.dto.LoginRequest;
import com.example.aiquiz.dto.LoginResponse;
import java.util.UUID;

@Service
public class LoginService {
    
    @Autowired
    private UserMapper userMapper;
    
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        // 生成简单的token
        String token = UUID.randomUUID().toString();
        return new LoginResponse(token, user.getUsername());
    }
} 