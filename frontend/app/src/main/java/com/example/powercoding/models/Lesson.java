package com.example.powercoding.models;

import com.google.gson.annotations.SerializedName;

public class Lesson {
    @SerializedName("lessonId")
    private long lessonId;

    @SerializedName("language")
    private String language;

    @SerializedName("title")
    private String title;

    @SerializedName("content")
    private String content;

    @SerializedName("iconResId")
    private int iconResId;

    public enum LessonState {ACTIVE, LOCKED, COMPLETED}

    @SerializedName("state")
    private LessonState state;

    @SerializedName("progress")
    private int progress;

    public Lesson(long lessonId, String language,
                  String title, String content,
                  int iconResId, LessonState state,
                  int progress) {
        this.lessonId = lessonId;
        this.language = language;
        this.title = title;
        this.content = content;
        this.iconResId = iconResId;
        this.state = state;
        this.progress = progress;
    }

    public long getLessonId() {
        return lessonId;
    }

    public void setLessonId(long lessonId) {
        this.lessonId = lessonId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public LessonState getState() {
        return state;
    }

    public void setState(LessonState state) {
        this.state = state;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
