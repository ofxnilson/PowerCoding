package com.example.powercoding;

public class Lesson {
    private int iconResId;
    private String title;

    public Lesson(int iconResId, String title) {
        this.iconResId = iconResId;
        this.title = title;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getTitle() {
        return title;
    }
}
