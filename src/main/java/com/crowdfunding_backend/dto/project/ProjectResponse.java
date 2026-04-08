package com.crowdfunding_backend.dto.project;

public class ProjectResponse {

  private Long id;
  private String title;
  private Double goalAmount;
  private Double currentAmount;
  private String creatorEmail;

  private Double remainingAmount;

  private Double totalEquityOffered;
  private Double equityAllocated;
  private Double remainingEquity;

  // ✅ DEFAULT CONSTRUCTOR (IMPORTANT)
  public ProjectResponse() {}

  // ✅ PARAMETERIZED CONSTRUCTOR (OPTIONAL)
  public ProjectResponse(Long id, String title, Double goalAmount,
                         Double currentAmount, String creatorEmail) {
    this.id = id;
    this.title = title;
    this.goalAmount = goalAmount;
    this.currentAmount = currentAmount;
    this.creatorEmail = creatorEmail;
  }

  // ✅ GETTERS
  public Long getId() { return id; }
  public String getTitle() { return title; }
  public Double getGoalAmount() { return goalAmount; }
  public Double getCurrentAmount() { return currentAmount; }
  public String getCreatorEmail() { return creatorEmail; }
  public Double getRemainingAmount() { return remainingAmount; }
  public Double getTotalEquityOffered() { return totalEquityOffered; }
  public Double getEquityAllocated() { return equityAllocated; }
  public Double getRemainingEquity() { return remainingEquity; }

  // ✅ SETTERS (🔥 ADD THESE)
  public void setId(Long id) { this.id = id; }
  public void setTitle(String title) { this.title = title; }
  public void setGoalAmount(Double goalAmount) { this.goalAmount = goalAmount; }
  public void setCurrentAmount(Double currentAmount) {
    this.currentAmount = currentAmount;
  }
  public void setCreatorEmail(String creatorEmail) {
    this.creatorEmail = creatorEmail;
  }

  public void setRemainingAmount(Double remainingAmount) {
    this.remainingAmount = remainingAmount;
  }
  public void setTotalEquityOffered(Double totalEquityOffered) {
    this.totalEquityOffered = totalEquityOffered;
  }
  public void setEquityAllocated(Double equityAllocated) {
    this.equityAllocated = equityAllocated;
  }
  public void setRemainingEquity(Double remainingEquity) {
    this.remainingEquity = remainingEquity;
  }
}