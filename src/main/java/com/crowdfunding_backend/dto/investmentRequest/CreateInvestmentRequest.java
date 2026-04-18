package com.crowdfunding_backend.dto.investmentRequest;

import lombok.Data;

@Data
public class CreateInvestmentRequest {
  private Long projectId;
  private Double amount;
  private Double equityPercentage;
}