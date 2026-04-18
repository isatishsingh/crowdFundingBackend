package com.crowdfunding_backend.dto.investmentRequest;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvestmentRequestResponse {

  private Long id;
  private Long projectId;
  private Double amount;
  private Double equityPercentage;
  private String status;
}