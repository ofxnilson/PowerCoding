package com.example.powercoding.models;

public class LessonQuestion {
    private String prompt;
    private String correctAnswer;

    public LessonQuestion(String prompt, String correctAnswer) {
        this.prompt = prompt;
        this.correctAnswer = correctAnswer;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }
}
