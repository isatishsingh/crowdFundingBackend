package com.crowdfunding_backend.dto.payment;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class CreateOrderResponse {
  private String orderId;
  private Double amount;
}
