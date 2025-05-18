package com.example.dailyflow_projet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Task {
    private String title;
    private String description;
    private String dueDate;
    private String priority;
    private boolean completed;
    private String userId;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public Task() {
        this.title = "";
        this.description = "";
        this.dueDate = "";
        this.priority = "";
        this.completed = false;
        this.userId = "";
    }

    public Task(String title, String description, String dueDate, String priority, boolean completed, String userId) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.completed = completed;
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getFormattedDueDate() {
        try {
            Date date = dateFormat.parse(dueDate);
            return date != null ? displayFormat.format(date) : dueDate;
        } catch (ParseException e) {
            return dueDate;
        }
    }

    public String getPriority() {
        return priority;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getUserId() {
        return userId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Helper method to validate and format date
    public static String formatDateForStorage(String dateStr) {
        try {
            Date date = displayFormat.parse(dateStr);
            return date != null ? dateFormat.format(date) : dateStr;
        } catch (ParseException e) {
            return dateStr;
        }
    }
}