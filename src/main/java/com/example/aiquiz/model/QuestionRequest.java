package com.example.aiquiz.model;

import java.util.List;

public class QuestionRequest {
    private String fileContent;
    private Integer questionCount;
    private List<String> types;
    private String subject;

    // Getters
    public String getFileContent() {
        return fileContent;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public List<String> getTypes() {
        return types;
    }

    public String getSubject() {
        return subject;
    }

    // Setters
    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
} 