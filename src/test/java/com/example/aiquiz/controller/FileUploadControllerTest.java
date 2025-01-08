package com.example.aiquiz.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartFile;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.example.aiquiz.entity.Question;
import com.example.aiquiz.entity.QuestionSet;
import com.example.aiquiz.service.QuestionSetService;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.util.Arrays;

@SpringBootTest
@AutoConfigureMockMvc
class FileUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private QuestionSetService questionSetService;
    
    @Test
    void testUploadFile() throws Exception {
        // 准备测试数据
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.pdf",
            "application/pdf",
            "测试内容".getBytes()
        );
        
        QuestionSet mockQuestionSet = QuestionSet.builder()
            .id(1L)
            .title("测试题目集")
            .description("测试描述")
            .questionCount(2)
            .questions(Arrays.asList(
                Question.builder()
                    .id(1L)
                    .type("选择题")
                    .content("测试题目1")
                    .build(),
                Question.builder()
                    .id(2L)
                    .type("判断题")
                    .content("测试题目2")
                    .build()
            ))
            .build();
            
        when(questionSetService.createQuestionSet(any(), any(), any()))
            .thenReturn(mockQuestionSet);
        
        // 执行测试
        mockMvc.perform(multipart("/api/upload")
                .file(file)
                .param("title", "测试题目集")
                .param("description", "测试描述"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("测试题目集"))
            .andExpect(jsonPath("$.questions").isArray())
            .andExpect(jsonPath("$.questions.length()").value(2));
    }
} 