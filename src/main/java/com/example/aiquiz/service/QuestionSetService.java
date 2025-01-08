package com.example.aiquiz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import com.example.aiquiz.model.QuestionSet;
import com.example.aiquiz.model.Question;
import com.example.aiquiz.mapper.QuestionSetMapper;
import com.example.aiquiz.mapper.QuestionMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

@Service
public class QuestionSetService {
    
    private static final Logger log = LoggerFactory.getLogger(QuestionSetService.class);
    private static final String FILES_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/files";
    private static final String CHAT_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    
    @Value("${ai.api.key}")
    private String apiKey;
    
    @Autowired
    private QuestionSetMapper questionSetMapper;
    
    @Autowired
    private QuestionMapper questionMapper;
    
    @Autowired
    private AIService aiService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
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
    
    @Transactional
    public QuestionSet createQuestionSet(MultipartFile file, int questionCount) {
        try {
            // 1. 从文件名生成标题
            String fileName = file.getOriginalFilename();
            if (fileName == null) {
                fileName = "未命名文件";
            }
            
            // 2. 先让AI生成文档描述
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of(
                "role", "system",
                "content", "fileid://" + uploadFile(file)
            ));
            messages.add(Map.of(
                "role", "user",
                "content", "请用一段话简要描述这个文档的主要内容。"
            ));
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "qwen-long");
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 500);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                CHAT_URL,
                HttpMethod.POST,
                request,
                String.class
            );
            
            // 解析文档描述
            JsonNode responseNode = objectMapper.readTree(response.getBody());
            String description = responseNode.get("choices")
                .get(0)
                .get("message")
                .get("content")
                .asText()
                .trim();
            
            // 4. 创建题目集合
            QuestionSet questionSet = new QuestionSet();
            questionSet.setTitle(fileName)
                      .setDescription(description)
                      .setQuestionCount(questionCount);
            
            questionSetMapper.insert(questionSet);
            
            // 5. 生成题目
            List<Question> questions = aiService.generateQuestionsFromFile(file, """
                请根据文档内容生成%d道题目，包括：
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
                题目：...
                A. ...
                B. ...
                C. ...
                D. ...
                答案：...
                解析：...
                
                【判断题1】
                题目：...
                答案：...（只能回答：正确/错误）
                解析：...
                
                【判断题2】
                题目：...
                答案：...（只能回答：正确/错误）
                解析：...
                
                【简答题】
                题目：...
                答案：...（请给出完整的答案）
                解析：无
                """.formatted(questionCount));
            
            // 6. 保存题目
            int questionNumber = 1;
            for (Question question : questions) {
                question.setSetId(questionSet.getId())
                       .setQuestionNumber(questionNumber++);
                questionMapper.insert(question);
            }
            
            questionSet.setQuestions(questions);
            return questionSet;
            
        } catch (Exception e) {
            log.error("创建题目集失败", e);
            throw new RuntimeException("创建题目集失败: " + e.getMessage());
        }
    }
    
    public QuestionSet getQuestionSet(Long id) {
        QuestionSet questionSet = questionSetMapper.findById(id);
        if (questionSet != null) {
            List<Question> questions = questionMapper.findBySetId(id);
            questionSet.setQuestions(questions);
        }
        return questionSet;
    }
    
    public List<QuestionSet> getAllQuestionSets() {
        return questionSetMapper.findAll();
    }
} 