package com.crowdfunding_backend.dto.investment;

import lombok.Data;

@Data
public class InvestmentRequest {
  private Long projectId;
  private Double amount;
}