package com.crowdfunding_backend.dto.investmentRequest;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvestmentRequestResponse {

  private Long id;
  private Long investorId;
  private Long projectId;
  private Double amount;
  private Double equityPercentage;
  private LocalDateTime investedAt;
  private String status;
}