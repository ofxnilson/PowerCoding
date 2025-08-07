package com.powercoding.model;

public class ProgressResponse {

    private Long userId;
    private String language;
    private int xp;
    private int streak;
    private int lives;
    private long livesTimestamp;
    private long lastActivityDate;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public long getLivesTimestamp() {
        return livesTimestamp;
    }

    public void setLivesTimestamp(long livesTimestamp) {
        this.livesTimestamp = livesTimestamp;
    }

    public long getLastActivityDate() {
        return lastActivityDate;
    }

    public void setLastActivityDate(long lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
    }

}
