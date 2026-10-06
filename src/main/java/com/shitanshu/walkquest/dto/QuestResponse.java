package com.shitanshu.walkquest.dto;

import java.util.List;

public class QuestResponse {

    private String title;
    private int duration;
    private List<String> activities;

    public QuestResponse() {
    }

    public QuestResponse(String title, int duration, List<String> activities) {
        this.title = title;
        this.duration = duration;
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

    public List<String> getActivities() {
        return activities;
    }

    public void setActivities(List<String> activities) {
        this.activities = activities;
    }
}
