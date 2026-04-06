package com.crowdfunding_backend.dto.investment;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class InvestmentResponse {

  private Long id;
  private Long investorId;
  private Long projectId;
  private Double amount;
  private LocalDateTime investedAt;
}