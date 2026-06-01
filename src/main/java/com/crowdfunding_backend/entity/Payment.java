package com.crowdfunding_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

  private Long userId;
  private Long projectId;

  private Double amount;

  private String status; // PENDING, SUCCESS, FAILED
  private Double equityPercentage;
  private Long investmentRequestId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentType paymentType = PaymentType.INVESTMENT;

  private String receiptNumber;

  private String razorpayOrderId;
  private String razorpayPaymentId;

  private LocalDateTime createdAt;
}
