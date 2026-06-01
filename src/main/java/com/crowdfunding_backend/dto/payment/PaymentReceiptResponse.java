package com.crowdfunding_backend.dto.payment;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentReceiptResponse {

  private Long paymentId;
  private String receiptNumber;
  private String razorpayOrderId;
  private String razorpayPaymentId;
  private Double amount;
  private String currency;
  private String status;
  private Long projectId;
  private String projectTitle;
  private Double equityPercentage;
  private String investorName;
  private String investorEmail;
  private LocalDateTime paidAt;
  private Long investmentId;
}
