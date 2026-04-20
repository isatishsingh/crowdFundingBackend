package com.crowdfunding_backend.repository;

import com.crowdfunding_backend.entity.ChatMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ChatMessageRepository
    extends JpaRepository<ChatMessage, Long> {

  List<ChatMessage>
  findByConversationIdOrderByCreatedAtAsc(Long conversationId);

  ChatMessage findTopByConversationIdOrderByCreatedAtDesc(Long conversationId);
}