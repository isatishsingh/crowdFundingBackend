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
@Table(name = "chat_message")

public class ChatMessage {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

  private Long conversationId;

  private Long senderId;

  private String message;

  private LocalDateTime createdAt;
}