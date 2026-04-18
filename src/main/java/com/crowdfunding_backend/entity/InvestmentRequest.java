package com.crowdfunding_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentRequest {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

  private Long investorId;
  private Long projectId;

  private Double amount;
  private Double equityPercentage;

  @Enumerated(EnumType.STRING) private Status status;

  private LocalDateTime createdAt;

  public enum Status { PENDING, APPROVED, REJECTED, COMPLETED }
}