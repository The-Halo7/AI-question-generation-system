package com.example.aiquiz.service;

import com.example.aiquiz.model.Question;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class AIService {
    private static final Logger log = LoggerFactory.getLogger(AIService.class);
    
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
                    请根据文档内容生成5道题目，包括2道选择题、2道判断题和1道简答题。
                    请严格按照以下格式输出，不要添加任何额外内容：
                    
                    【选择题1】
                    题目：<在此处填写题目>
                    A. <选项A>
                    B. <选项B>
                    C. <选项C>
                    D. <选项D>
                    答案：<A/B/C/D>
                    解析：<解析内容>
                    
                    【选择题2】
                    题目：<在此处填写题目>
                    A. <选项A>
                    B. <选项B>
                    C. <选项C>
                    D. <选项D>
                    答案：<A/B/C/D>
                    解析：<解析内容>
                    
                    【判断题1】
                    题目：<在此处填写题目>
                    答案：<只能填写：正确/错误>
                    解析：<解析内容>
                    
                    【判断题2】
                    题目：<在此处填写题目>
                    答案：<只能填写：正确/错误>
                    解析：<解析内容>
                    
                    【简答题】
                    题目：<在此处填写题目>
                    答案：<完整的答案内容>
                    解析：无
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
            
            List<Question> questions = parseAIResponseToQuestions(response.getBody());
            if (questions.isEmpty()) {
                log.error("未能生成任何题目，AI响应：{}", response.getBody());
                throw new RuntimeException("未能生成题目");
            }
            return questions;
            
        } catch (Exception e) {
            log.error("AI服务调用失败", e);
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
            log.debug("开始解析AI响应: {}", aiResponse);
            
            JsonNode responseNode = objectMapper.readTree(aiResponse);
            String content = responseNode.get("choices")
                .get(0)
                .get("message")
                .get("content")
                .asText();
            
            log.debug("提取的内容: {}", content);
            
            List<Question> questions = new ArrayList<>();
            String[] questionBlocks = content.split("(?=【选择题[12]】|【判断题[12]】|【简答题】)");
            
            for (String block : questionBlocks) {
                if (block.trim().isEmpty()) continue;
                
                log.debug("处理题目块: {}", block);
                Question question = parseQuestionBlock(block.trim());
                if (question != null) {
                    questions.add(question);
                    log.debug("成功解析题目: type={}, content={}", question.getType(), question.getContent());
                } else {
                    log.error("题目块解析失败: {}", block);
                }
            }
            
            return questions;
            
        } catch (Exception e) {
            log.error("解析AI响应失败", e);
            throw new RuntimeException("解析AI响应失败: " + e.getMessage());
        }
    }
    
    private Question parseQuestionBlock(String block) {
        try {
            String[] lines = block.split("\n");
            String type = null;
            String content = "";
            String answer = "";
            String analysis = "";
            StringBuilder contentBuilder = new StringBuilder();
            
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                if (line.contains("【选择题")) {
                    type = "选择题";
                } else if (line.contains("【判断题")) {
                    type = "判断题";
                } else if (line.contains("【简答题】")) {
                    type = "简答题";
                } else if (line.startsWith("题目：")) {
                    contentBuilder.append(line.substring(3)).append("\n");
                } else if (line.startsWith("A.") || line.startsWith("B.") || 
                          line.startsWith("C.") || line.startsWith("D.")) {
                    contentBuilder.append(line).append("\n");
                } else if (line.startsWith("答案：")) {
                    answer = line.substring(3).trim();
                } else if (line.startsWith("解析：")) {
                    analysis = line.substring(3).trim();
                }
            }
            
            content = contentBuilder.toString().trim();
            
            if (type == null || content.isEmpty() || answer.isEmpty()) {
                log.error("题目解析失败，缺少必要字段: type={}, content={}, answer={}", type, content, answer);
                return null;
            }
            
            if (type.equals("简答题")) {
                analysis = "无";
            }
            
            Question question = new Question();
            question.setType(type)
                    .setContent(content)
                    .setAnswer(answer)
                    .setAnalysis(analysis);
            
            return question;
            
        } catch (Exception e) {
            log.error("解析题目块失败: {}", block, e);
            return null;
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