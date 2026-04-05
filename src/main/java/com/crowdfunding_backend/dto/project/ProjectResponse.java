package com.crowdfunding_backend.dto.project;

public class ProjectResponse {

  private Long id;
  private String title;
  private Double goalAmount;
  private Double currentAmount;
  private String creatorEmail;

  public ProjectResponse(Long id, String title, Double goalAmount,
                         Double currentAmount, String creatorEmail) {
    this.id = id;
    this.title = title;
    this.goalAmount = goalAmount;
    this.currentAmount = currentAmount;
    this.creatorEmail = creatorEmail;
  }

  // getters
  public Long getId() { return id; }

  public String getTitle() { return title; }

  public Double getGoalAmount() { return goalAmount; }

  public Double getCurrentAmount() { return currentAmount; }

  public String getCreatorEmail() { return creatorEmail; }
}