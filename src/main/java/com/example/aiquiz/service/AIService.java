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
                "content", prompt
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
            JsonNode responseNode = objectMapper.readTree(aiResponse);
            String content = responseNode.get("choices")
                .get(0)
                .get("message")
                .get("content")
                .asText();
            
            List<Question> questions = new ArrayList<>();
            String[] questionBlocks = content.split("(?=【选择题[12]】|【判断题[12]】|【简答题】)");
            
            int questionNumber = 1;  // 用于生成序号
            for (String block : questionBlocks) {
                if (block.trim().isEmpty()) continue;
                
                Question question = parseQuestionBlock(block.trim(), questionNumber++);
                if (question != null) {
                    questions.add(question);
                }
            }
            
            return questions;
        } catch (Exception e) {
            throw new RuntimeException("解析AI响应失败: " + e.getMessage());
        }
    }
    

    private Question parseQuestionBlock(String block, int questionNumber) {
        try {
            String type;
            if (block.contains("【选择题")) {
                type = "选择题";
            } else if (block.contains("【判断题")) {
                type = "判断题";
            } else if (block.contains("【简答题】")) {
                type = "简答题";
            } else {
                return null;
            }
            
            String content = "";
            String answer = "";
            String analysis = "";
            
            // 分行处理
            String[] lines = block.split("\n");
            StringBuilder contentBuilder = new StringBuilder();
            StringBuilder questionBuilder = new StringBuilder();
            boolean isContent = true;  // 默认是内容
            
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                if (line.startsWith("题目：")) {
                    questionBuilder.append(line.substring(3)).append("\n");
                } else if (line.startsWith("答案：")) {
                    isContent = false;
                    answer = line.substring(3).trim();
                } else if (line.startsWith("解析：")) {
                    isContent = false;
                    analysis = line.substring(3).trim();
                } else if (line.startsWith("A.") || line.startsWith("B.") || 
                          line.startsWith("C.") || line.startsWith("D.")) {
                    contentBuilder.append(line).append("\n");
                } else if (line.startsWith("---") || line.startsWith("###")) {
                    // 忽略分隔符
                    continue;
                } else {
                    if (isContent) {
                        if (line.startsWith("【")) {
                            // 忽略题目类型标记
                            continue;
                        }
                        contentBuilder.append(line).append("\n");
                    }
                }
            }
            
            // 组合题目内容
            if (questionBuilder.length() > 0) {
                content = questionBuilder.toString().trim() + "\n" + contentBuilder.toString().trim();
            } else {
                content = contentBuilder.toString().trim();
            }
            
            // 处理可能包含在content中的答案和解析
            int answerIndex = content.indexOf("\n答案：");
            if (answerIndex > 0) {
                String temp = content;
                content = temp.substring(0, answerIndex).trim();
                
                // 提取答案
                if (answer.isEmpty()) {
                    int analysisIndex = temp.indexOf("\n解析：", answerIndex);
                    if (analysisIndex > 0) {
                        answer = temp.substring(answerIndex + 4, analysisIndex).trim();
                        if (type.equals("简答题")) {
                            analysis = "无";  // 简答题固定解析为"无"
                        } else {
                            analysis = temp.substring(analysisIndex + 4).trim();
                        }
                    } else {
                        answer = temp.substring(answerIndex + 4).trim();
                        if (type.equals("简答题")) {
                            analysis = "无";
                        }
                    }
                }
            }
            
            // 创建题目对象
            Question question = new Question();
            question.setType(type)
                   .setContent(content)
                   .setAnswer(answer)
                   .setAnalysis(analysis);
            
            return question;
            
        } catch (Exception e) {
            System.out.println("解析题目块失败: " + block);
            e.printStackTrace();
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