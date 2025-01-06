package com.example.aiquiz.controller;

import com.example.aiquiz.model.Question;
import com.example.aiquiz.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController
@RequestMapping("/api/test")
@Slf4j
public class TestController {

    @Autowired
    private AIService aiService;

    @GetMapping("/ai")
    public List<Question> testAI() {
        String prompt = "请生成一道计算机网络相关的选择题，包含题目、选项、答案和解析。";
        try {
            return aiService.generateQuestions(prompt);
        } catch (Exception e) {
            throw new RuntimeException("测试失败: " + e.getMessage());
        }
    }
} 