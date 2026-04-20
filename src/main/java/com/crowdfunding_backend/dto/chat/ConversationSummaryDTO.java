package com.crowdfunding_backend.dto.chat;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationSummaryDTO {
  private Long conversationId;
  private Long projectId;
  private String projectTitle;
  private Long receiverId;
  private String receiverName;
  private String lastMessage;
  private LocalDateTime lastMessageAt;
}
