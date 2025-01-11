package com.example.aiquiz.model;

import java.time.LocalDateTime;

public class User {
    private Long id;
    private String username;
    private String password;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // Getters and Setters
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public LocalDateTime getCreateTime() { return createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    
    public void setId(Long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
} 