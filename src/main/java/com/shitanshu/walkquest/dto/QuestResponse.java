package com.shitanshu.walkquest.dto;

import java.util.List;

public class QuestResponse {

    private String title;
    private int duration;
    private String category;
    private String difficulty;
    private List<String> activities;

    public QuestResponse() {
    }

    public QuestResponse(
            String title,
            int duration,
            String category,
            String difficulty,
            List<String> activities
    ) {
        this.title = title;
        this.duration = duration;
        this.category = category;
        this.difficulty = difficulty;
        this.activities = activities;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public List<String> getActivities() {
        return activities;
    }

    public void setActivities(List<String> activities) {
        this.activities = activities;
    }
}
