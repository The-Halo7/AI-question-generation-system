package com.example.aiquiz.dto;

public class QuestionCountConfig {
    private int choiceCount;     // 选择题数量
    private int judgmentCount;   // 判断题数量
    private int shortAnswerCount; // 简答题数量
    
    // 根据总数和默认比例生成配置
    public static QuestionCountConfig createDefault(int totalCount) {
        QuestionCountConfig config = new QuestionCountConfig();
        config.choiceCount = (int) Math.round(totalCount * 0.5);      // 50%
        config.judgmentCount = (int) Math.round(totalCount * 0.3);    // 30%
        config.shortAnswerCount = totalCount - config.choiceCount - config.judgmentCount; // 剩余部分
        return config;
    }
    
    // Getters and Setters
    public int getChoiceCount() { return choiceCount; }
    public int getJudgmentCount() { return judgmentCount; }
    public int getShortAnswerCount() { return shortAnswerCount; }
    
    public void setChoiceCount(int choiceCount) { this.choiceCount = choiceCount; }
    public void setJudgmentCount(int judgmentCount) { this.judgmentCount = judgmentCount; }
    public void setShortAnswerCount(int shortAnswerCount) { this.shortAnswerCount = shortAnswerCount; }
} 