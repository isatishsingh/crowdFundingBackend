package com.crowdfunding_backend.dto.chat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageDTO {

  private Long receiverId;
  private Long projectId;
  private String message;

  // ❌ REMOVE senderId completely
}