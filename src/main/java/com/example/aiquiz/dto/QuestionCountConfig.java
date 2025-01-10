package com.example.aiquiz.dto;

public class QuestionCountConfig {
    private int choiceCount;     // 选择题数量
    private int judgmentCount;   // 判断题数量
    private int shortAnswerCount; // 简答题数量
    
    // 根据总数生成默认配置
    public static QuestionCountConfig createDefault(int totalCount) {
        QuestionCountConfig config = new QuestionCountConfig();
        if (totalCount <= 5) {
            config.choiceCount = 2;      // 2道选择题
            config.judgmentCount = 2;    // 2道判断题
            config.shortAnswerCount = 1;  // 1道简答题
        } else {
            // 如果总数大于5，保持相同比例
            config.choiceCount = (int) Math.round(totalCount * 0.4);      // 40%
            config.judgmentCount = (int) Math.round(totalCount * 0.4);    // 40%
            config.shortAnswerCount = totalCount - config.choiceCount - config.judgmentCount; // 20%
        }
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