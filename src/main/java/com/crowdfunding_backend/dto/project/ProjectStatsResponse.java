package com.crowdfunding_backend.dto.project;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectStatsResponse {
  private Long investorCount;
  private Integer likes;
  private Integer shares;
  private Integer commentsCount;
}
