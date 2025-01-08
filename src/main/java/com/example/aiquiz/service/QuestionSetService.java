package com.example.aiquiz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.example.aiquiz.model.QuestionSet;
import com.example.aiquiz.model.Question;
import com.example.aiquiz.mapper.QuestionSetMapper;
import com.example.aiquiz.mapper.QuestionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@Service
public class QuestionSetService {
    
    private static final Logger log = LoggerFactory.getLogger(QuestionSetService.class);
    
    @Autowired
    private QuestionSetMapper questionSetMapper;
    
    @Autowired
    private QuestionMapper questionMapper;
    
    @Autowired
    private AIService aiService;
    
    @Transactional
    public QuestionSet createQuestionSet(MultipartFile file, int questionCount) {
        try {
            // 1. 从文件名生成标题
            String fileName = file.getOriginalFilename();
            String title = fileName != null ? 
                fileName.substring(0, fileName.lastIndexOf('.')) : 
                "未命名题目集";
            
            // 2. 创建题目集合
            QuestionSet questionSet = new QuestionSet()
                .setTitle(title)
                .setDescription("基于文件 " + fileName + " 生成的题目")
                .setQuestionCount(questionCount);
            
            questionSetMapper.insert(questionSet);
            
            // 3. 生成题目
            String prompt = String.format("请根据文件内容生成%d道题目。", questionCount);
            List<Question> questions = aiService.generateQuestionsFromFile(file, prompt);
            
            // 4. 保存题目
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