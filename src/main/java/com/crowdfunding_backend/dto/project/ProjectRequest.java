package com.crowdfunding_backend.dto.project;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public class ProjectRequest {

  private String title;
  private String description;
  private Double goalAmount;
  private LocalDateTime deadline;

  @NotNull @Positive private Double totalEquityOffered; // 🔥 ADD THIS

  // getters & setters
  public Double getTotalEquityOffered() { return totalEquityOffered; }

  public void setTotalEquityOffered(Double totalEquityOffered) {
    this.totalEquityOffered = totalEquityOffered;
  }

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