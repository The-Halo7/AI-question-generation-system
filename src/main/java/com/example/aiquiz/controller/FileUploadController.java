package com.example.aiquiz.controller;

import com.example.aiquiz.model.Question;
import com.example.aiquiz.model.QuestionSet;
import com.example.aiquiz.service.AIService;
import com.example.aiquiz.service.QuestionSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;
import com.example.aiquiz.dto.QuestionCountConfig;

import java.util.List;

@RestController
@RequestMapping("/api/upload")
@Slf4j
public class FileUploadController {
    
    @Autowired
    private QuestionSetService questionSetService;
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(
            @RequestHeader("Authorization") String token,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "totalCount", required = false) Integer totalCount,
            @RequestParam(value = "choiceCount", required = false) Integer choiceCount,
            @RequestParam(value = "judgmentCount", required = false) Integer judgmentCount,
            @RequestParam(value = "shortAnswerCount", required = false) Integer shortAnswerCount) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("请选择文件");
            }
            
            String fileName = file.getOriginalFilename();
            if (fileName == null) {
                return ResponseEntity.badRequest().body("文件名不能为空");
            }
            
            String fileExtension = fileName.toLowerCase();
            if (!fileExtension.endsWith(".pdf") && 
                !fileExtension.endsWith(".doc") && 
                !fileExtension.endsWith(".docx")) {
                return ResponseEntity.badRequest().body("不支持的文件格式，仅支持 PDF 和 Word 文件");
            }
            
            QuestionCountConfig config;
            if (totalCount != null) {
                // 如果提供了总数，使用默认比例
                config = QuestionCountConfig.createDefault(totalCount);
            } else if (choiceCount != null || judgmentCount != null || shortAnswerCount != null) {
                // 如果提供了任意类型的数量，使用指定的数量
                config = new QuestionCountConfig();
                config.setChoiceCount(choiceCount != null ? choiceCount : 0);
                config.setJudgmentCount(judgmentCount != null ? judgmentCount : 0);
                config.setShortAnswerCount(shortAnswerCount != null ? shortAnswerCount : 0);
            } else {
                // 如果什么都没提供，使用默认的5道题
                config = QuestionCountConfig.createDefault(5);
            }
            
            QuestionSet questionSet = questionSetService.createQuestionSet(file, config);
            return ResponseEntity.ok(questionSet);
            
        } catch (Exception e) {
            log.error("文件处理失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("文件处理失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/sets")
    public ResponseEntity<List<QuestionSet>> getAllQuestionSets() {
        return ResponseEntity.ok(questionSetService.getAllQuestionSets());
    }
    
    @GetMapping("/sets/{id}")
    public ResponseEntity<?> getQuestionSet(@PathVariable Long id) {
        QuestionSet questionSet = questionSetService.getQuestionSet(id);
        if (questionSet == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(questionSet);
    }
} 