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
public class PaymentTransaction {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

  private Long paymentId;

  private String transactionType; // CREDIT / DEBIT / REFUND

  private Double amount;

  private String status;

  private LocalDateTime createdAt;
}