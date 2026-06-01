package com.crowdfunding_backend.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class UpdateProjectRequest {

  @NotBlank(message = "Project title is required")
  private String title;

  @NotBlank(message = "Description is required")
  private String description;

  @NotNull(message = "Deadline is required")
  private LocalDateTime deadline;

  public String getTitle() { return title; }

  public String getDescription() { return description; }

  public LocalDateTime getDeadline() { return deadline; }

  public void setTitle(String title) { this.title = title; }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
}
