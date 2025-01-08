package com.example.aiquiz.model;

import java.time.LocalDateTime;

public class Question {
    private Long id;
    private Long setId;
    private Integer questionNumber;
    private String type;
    private String content;
    private String answer;
    private String analysis;
    private LocalDateTime createTime;
    
    public Question() {}
    
    // Getters
    public Long getId() { return id; }
    public Long getSetId() { return setId; }
    public Integer getQuestionNumber() { return questionNumber; }
    public String getType() { return type; }
    public String getContent() { return content; }
    public String getAnswer() { return answer; }
    public String getAnalysis() { return analysis; }
    public LocalDateTime getCreateTime() { return createTime; }
    
    // Setters
    public Question setId(Long id) { this.id = id; return this; }
    public Question setSetId(Long setId) { this.setId = setId; return this; }
    public Question setQuestionNumber(Integer questionNumber) { this.questionNumber = questionNumber; return this; }
    public Question setType(String type) { this.type = type; return this; }
    public Question setContent(String content) { this.content = content; return this; }
    public Question setAnswer(String answer) { this.answer = answer; return this; }
    public Question setAnalysis(String analysis) { this.analysis = analysis; return this; }
    public Question setCreateTime(LocalDateTime createTime) { this.createTime = createTime; return this; }
} 