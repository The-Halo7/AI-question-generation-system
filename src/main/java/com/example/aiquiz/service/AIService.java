package com.example.aiquiz.service;

import com.example.aiquiz.model.Question;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import com.fasterxml.jackson.databind.JsonNode;

@Service
@Slf4j
public class AIService {
    
    @Value("${ai.api.key}")
    private String apiKey;
    
    private final String FILES_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/files";
    private final String CHAT_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public List<Question> generateQuestionsFromFile(MultipartFile file, String prompt) {
        try {
            // 1. 上传文件
            String fileId = uploadFile(file);
            
            // 2. 使用文件ID生成题目
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of(
                "role", "system",
                "content", "fileid://" + fileId
            ));
            messages.add(Map.of(
                "role", "user",
                "content", """
                    请根据文档内容生成5道题目，包括：
                    1. 2道选择题（每题4个选项）
                    2. 2道判断题
                    3. 1道简答题
                    
                    请按以下格式输出每道题：
                    
                    【选择题1】
                    题目：...
                    A. ...
                    B. ...
                    C. ...
                    D. ...
                    答案：...
                    解析：...
                    
                    【选择题2】
                    ...
                    
                    【判断题1】
                    ...
                    
                    【判断题2】
                    ...
                    
                    【简答题】
                    ...
                    """
            ));
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "qwen-long");
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2000);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                CHAT_URL,
                HttpMethod.POST,
                request,
                String.class
            );
            
            return parseAIResponseToQuestions(response.getBody());
            
        } catch (Exception e) {
            throw new RuntimeException("AI服务调用失败: " + e.getMessage());
        }
    }
    
    private String uploadFile(MultipartFile file) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Authorization", "Bearer " + apiKey);
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });
        body.add("purpose", "file-extract");
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(
            FILES_URL,
            HttpMethod.POST,
            requestEntity,
            String.class
        );
        
        JsonNode responseNode = objectMapper.readTree(response.getBody());
        return responseNode.get("id").asText();
    }
    
    private List<Question> parseAIResponseToQuestions(String aiResponse) {
        try {
            // 解析 AI 响应
            JsonNode responseNode = objectMapper.readTree(aiResponse);
            String content = responseNode.get("choices")
                .get(0)
                .get("message")
                .get("content")
                .asText();
            
            // TODO: 实现更详细的题目解析逻辑
            List<Question> questions = new ArrayList<>();
            Question question = Question.builder()
                .content(content)
                .type("AI生成题目")
                .answer("AI生成答案")
                .analysis("AI生成解析")
                .difficulty(1)
                .subject("通用")
                .build();
            questions.add(question);
            return questions;
        } catch (Exception e) {
//            log.error("AI响应内容: {}", aiResponse);  // 添加日志输出
            throw new RuntimeException("解析AI响应失败: " + e.getMessage());
        }
    }
    
    private Map<String, String> createMessage(String role, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }
    
    public List<Question> generateQuestions(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(createMessage("system", "你是一个专业的教育工作者，善于出题考核学生对知识的掌握程度。"));
            messages.add(createMessage("user", prompt));
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "qwen-long");
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2000);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                CHAT_URL,
                HttpMethod.POST,
                request,
                String.class
            );
            
            return parseAIResponseToQuestions(response.getBody());
            
        } catch (Exception e) {
            throw new RuntimeException("AI服务调用失败: " + e.getMessage());
        }
    }
} 