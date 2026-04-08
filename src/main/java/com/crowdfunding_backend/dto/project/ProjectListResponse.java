package com.crowdfunding_backend.dto.project;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectListResponse {

  private Long id;
  private String title;
  private String description;

  private Double goalAmount;
  private Double currentAmount;
  private Double totalEquityOffered;
  private Double equityAllocated;
  private Double remainingEquity;
  private LocalDateTime deadline;

  private Long creatorId;

  // 🔥 Optional (recommended)
  private Double fundingPercentage;
}