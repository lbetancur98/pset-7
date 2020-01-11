package com.apcsa.model;

public class Assignment {
    private String title;
    private int assignmentId;
    private int pointValue;

    public Assignment (String title, int assignmentId, int pointValue) {
        this.title = title;
        this.assignmentId = assignmentId;
        this.pointValue = pointValue;
    }

    public String getTitle() {
        return this.title;
    }

    public int getAssignmentId() {
        return this.assignmentId;
    }

    public int getPointValue() {
        return this.pointValue;
    }
}