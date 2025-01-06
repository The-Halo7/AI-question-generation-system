package com.example.aiquiz.controller;

import com.example.aiquiz.model.Question;
import com.example.aiquiz.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController
@RequestMapping("/api/upload")
@Slf4j
public class FileUploadController {
    
    @Autowired
    private AIService aiService;
    
    @PostMapping
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "questionCount", defaultValue = "5") int questionCount) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("请选择文件");
            }
            
            String fileName = file.getOriginalFilename();
            if (fileName == null) {
                return ResponseEntity.badRequest().body("文件名不能为空");
            }
            
            String fileExtension = fileName.toLowerCase();
            if (fileExtension.endsWith(".ppt") || fileExtension.endsWith(".pptx")) {
                return ResponseEntity.badRequest().body("暂不支持PPT格式，请上传PDF或Word文档");
            }
            
            if (!fileExtension.endsWith(".pdf") && 
                !fileExtension.endsWith(".doc") && 
                !fileExtension.endsWith(".docx")) {
                return ResponseEntity.badRequest().body("不支持的文件格式，仅支持 PDF 和 Word 文件");
            }
            
            String prompt = String.format("请根据文件内容生成%d道题目。", questionCount);
            List<Question> questions = aiService.generateQuestionsFromFile(file, prompt);
            return ResponseEntity.ok(questions);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("文件处理失败: " + e.getMessage());
        }
    }
} 