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
@Table(name = "conversation",
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"user1_id", "user2_id", "project_id"}))

public class Conversation {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

  private Long user1Id;
  private Long user2Id;

  private Long projectId;

  private LocalDateTime createdAt;
}