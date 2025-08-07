package com.powercoding.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lesson_id")
    private Long lessonId;

    @Column(nullable = false)
    private String language;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String content;

    @Column(nullable = false)
    private boolean unlocked = false;

    private int iconResId;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL)
    private List<LessonQuestion> questions = new ArrayList<>();

    public List<LessonQuestion> getQuestions() { 
        return questions; 
    }

    public void setQuestions(List<LessonQuestion> questions) { 
        this.questions = questions; 
    }

    public Lesson() {
    }

    public Lesson(String language, String title, String content, boolean unlocked) {
        this.language = language;
        this.title = title;
        this.content = content;
        this.unlocked = unlocked;
    }

    public Lesson(Long lessonId, String language, String title, String content, boolean unlocked) {
        this.lessonId = lessonId;
        this.language = language;
        this.title = title;
        this.content = content;
        this.unlocked = unlocked;
    }

    public Long getLessonId() {
        return lessonId;
    }

    public void setLessonId(Long lessonId) {
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

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

        public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

}
