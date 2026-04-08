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
@Table(name = "investments")
public class Investment {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

  @ManyToOne
  @JoinColumn(name = "investor_id", nullable = false)
  private User investor;

  @ManyToOne
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  @Column(nullable = false) private Double amount;

  @Column(name = "equity_percentage", nullable = false)
  private Double equityPercentage;

  @Column(name = "invested_at") private LocalDateTime investedAt;

  @PrePersist
  public void prePersist() {
    this.investedAt = LocalDateTime.now();
  }
}