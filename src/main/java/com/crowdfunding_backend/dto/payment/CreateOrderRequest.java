package com.crowdfunding_backend.dto.payment;
import lombok.*;

@Getter
@Setter
public class CreateOrderRequest {
  private Long projectId;
  private Double amount;
  private Double equityPercentage;
}
