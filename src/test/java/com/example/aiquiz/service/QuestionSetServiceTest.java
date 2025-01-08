package com.example.aiquiz.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class QuestionSetServiceTest {

    @Autowired
    private QuestionSetService questionSetService;
    
    @MockBean
    private AIService aiService;
    
    @Test
    void testCreateQuestionSet() {
        // 准备测试数据
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.pdf",
            "application/pdf",
            "测试内容".getBytes()
        );
        
        // 模拟AI服务返回
        List<Question> mockQuestions = Arrays.asList(
            Question.builder()
                .type("选择题")
                .content("测试题目1")
                .answer("A")
                .analysis("解析1")
                .build(),
            Question.builder()
                .type("判断题")
                .content("测试题目2")
                .answer("正确")
                .analysis("解析2")
                .build()
        );
        
        when(aiService.generateQuestionsFromFile(any(), any()))
            .thenReturn(mockQuestions);
        
        // 执行测试
        QuestionSet result = questionSetService.createQuestionSet(
            file, 
            "测试题目集", 
            "测试描述"
        );
        
        // 验证结果
        assertNotNull(result);
        assertEquals("测试题目集", result.getTitle());
        assertEquals("测试描述", result.getDescription());
        assertEquals(2, result.getQuestions().size());
    }
} 