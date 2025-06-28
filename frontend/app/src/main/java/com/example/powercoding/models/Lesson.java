package com.example.powercoding.models;

public class Lesson {
    private Long lessonId;
    private String language;
    private String title;
    private String content;
    private int iconResId;
    private LessonState state;

    public enum LessonState { ACTIVE, LOCKED, COMPLETED }

    public Lesson(Long lessonId, String language, String title, String content, int iconResId, LessonState state) {
        this.lessonId = lessonId;
        this.language = language;
        this.title = title;
        this.content = content;
        this.iconResId = iconResId;
        this.state = state;
    }
    // Getters and setters...
    public Long getLessonId() {
        return lessonId;
    }
    public String getLanguage() {
        return language;
    }
    public String getTitle() {
        return title;
    }
    public String getContent() {
        return content;
    }
    public int getIconResId() {
        return iconResId;
    }
    public LessonState getState() {
        return state;
    }
    public void setState(LessonState state) {
        this.state = state;
    }
}

