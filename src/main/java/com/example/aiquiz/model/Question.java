package com.example.aiquiz.model;

public class Question {
    private Long id;
    private String content;
    private String type;
    private String answer;
    private String analysis;
    private Integer difficulty;
    private String subject;

    // Getters
    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getType() {
        return type;
    }

    public String getAnswer() {
        return answer;
    }

    public String getAnalysis() {
        return analysis;
    }

    public Integer getDifficulty() {
        return difficulty;
    }

    public String getSubject() {
        return subject;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }

    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    // Builder
    public static QuestionBuilder builder() {
        return new QuestionBuilder();
    }

    public static class QuestionBuilder {
        private final Question question = new Question();

        public QuestionBuilder content(String content) {
            question.content = content;
            return this;
        }

        public QuestionBuilder type(String type) {
            question.type = type;
            return this;
        }

        public QuestionBuilder answer(String answer) {
            question.answer = answer;
            return this;
        }

        public QuestionBuilder analysis(String analysis) {
            question.analysis = analysis;
            return this;
        }

        public QuestionBuilder difficulty(Integer difficulty) {
            question.difficulty = difficulty;
            return this;
        }

        public QuestionBuilder subject(String subject) {
            question.subject = subject;
            return this;
        }

        public Question build() {
            return question;
        }
    }
} 