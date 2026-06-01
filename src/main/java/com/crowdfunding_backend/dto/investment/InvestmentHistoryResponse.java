package com.crowdfunding_backend.dto.investment;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvestmentHistoryResponse {

  private Long investmentId;
  private Long projectId;
  private String projectTitle;
  private Double amountInvested;
  private Double equityOwned;
  private LocalDateTime investmentDate;
  private Long paymentId;
  private String receiptNumber;
  private String razorpayPaymentId;
}
