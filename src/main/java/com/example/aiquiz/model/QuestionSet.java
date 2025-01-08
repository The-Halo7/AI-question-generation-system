package com.example.aiquiz.model;

import java.time.LocalDateTime;
import java.util.List;

public class QuestionSet {
    private Long id;
    private String title;
    private String description;
    private Integer questionCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<Question> questions;
    
    public QuestionSet() {}
    
    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Integer getQuestionCount() { return questionCount; }
    public LocalDateTime getCreateTime() { return createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public List<Question> getQuestions() { return questions; }
    
    // Setters
    public QuestionSet setId(Long id) { this.id = id; return this; }
    public QuestionSet setTitle(String title) { this.title = title; return this; }
    public QuestionSet setDescription(String description) { this.description = description; return this; }
    public QuestionSet setQuestionCount(Integer questionCount) { this.questionCount = questionCount; return this; }
    public QuestionSet setCreateTime(LocalDateTime createTime) { this.createTime = createTime; return this; }
    public QuestionSet setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; return this; }
    public QuestionSet setQuestions(List<Question> questions) { this.questions = questions; return this; }
} 