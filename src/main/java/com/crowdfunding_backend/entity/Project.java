package com.crowdfunding_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Project {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

  private String title;

  @Column(length = 2000) private String description;

  private Double goalAmount;
  private Double currentAmount = 0.0;

  private LocalDateTime deadline;
  private LocalDateTime createdAt;

  @ManyToOne @JoinColumn(name = "creator_id") private User creator;

  // ✅ GETTERS

  public Long getId() { return id; }

  public String getTitle() { return title; }

  public String getDescription() { return description; }

  public Double getGoalAmount() { return goalAmount; }

  public Double getCurrentAmount() { return currentAmount; }

  public LocalDateTime getDeadline() { return deadline; }

  public LocalDateTime getCreatedAt() { return createdAt; }

  public User getCreator() { return creator; }

  // ✅ SETTERS

  public void setId(Long id) { this.id = id; }

  public void setTitle(String title) { this.title = title; }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setGoalAmount(Double goalAmount) { this.goalAmount = goalAmount; }

  public void setCurrentAmount(Double currentAmount) {
    this.currentAmount = currentAmount;
  }

  public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public void setCreator(User creator) { this.creator = creator; }
}