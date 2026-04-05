package com.crowdfunding_backend.dto.project;

import java.time.LocalDateTime;

public class ProjectRequest {

  private String title;
  private String description;
  private Double goalAmount;
  private LocalDateTime deadline;

  // getters & setters
  public String getTitle() { return title; }

  public String getDescription() { return description; }

  public Double getGoalAmount() { return goalAmount; }

  public LocalDateTime getDeadline() { return deadline; }

  // ✅ SETTERS

  public void setTitle(String title) { this.title = title; }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setGoalAmount(Double goalAmount) { this.goalAmount = goalAmount; }

  public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
}