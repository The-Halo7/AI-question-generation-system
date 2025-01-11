package com.example.aiquiz.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.aiquiz.service.LoginService;
import com.example.aiquiz.dto.LoginRequest;
import com.example.aiquiz.dto.LoginResponse;

@RestController
@RequestMapping("/api")
public class LoginController {
    
    @Autowired
    private LoginService loginService;
    
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = loginService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
