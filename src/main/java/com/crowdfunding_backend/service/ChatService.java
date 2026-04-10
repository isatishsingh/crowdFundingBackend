package com.crowdfunding_backend.service;

import com.crowdfunding_backend.entity.*;
import com.crowdfunding_backend.repository.*;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

  @Autowired private ConversationRepository conversationRepository;
  @Autowired private ChatMessageRepository chatRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private ProjectRepository projectRepository;

  public Conversation getOrCreateConversation(Long user1Id, Long user2Id,
                                              Long projectId) {

    // 🔥 STEP 1: Always sort users
    Long smaller = Math.min(user1Id, user2Id);
    Long larger = Math.max(user1Id, user2Id);

    // 🔥 STEP 2: Find existing conversation
    return conversationRepository
        .findByUser1IdAndUser2IdAndProjectId(smaller, larger, projectId)
        .orElseGet(() -> {
          // 🔥 STEP 3: Create new if not exists
          Conversation conversation = Conversation.builder()
                                          .user1Id(smaller)
                                          .user2Id(larger)
                                          .projectId(projectId)
                                          .createdAt(LocalDateTime.now())
                                          .build();

          return conversationRepository.save(conversation);
        });
  }

  public ChatMessage sendMessage(String email, Long receiverId, Long projectId,
                                 String message) {

    User sender = userRepository.findByEmailIgnoreCase(email).orElseThrow(
        () -> new RuntimeException("User not found"));

    // ✅ Check receiver exists
    User receiver =
        userRepository.findById(receiverId)
            .orElseThrow(() -> new RuntimeException("Receiver not found"));

    // ✅ Check project exists
    Project project = projectRepository.findById(projectId).orElseThrow(
        () -> new RuntimeException("Project not found"));

    // 🔥 OPTIONAL: Prevent self messaging
    if (sender.getId().equals(receiverId)) {
      throw new RuntimeException("Cannot send message to yourself");
    }

    Conversation conversation =
        getOrCreateConversation(sender.getId(), receiverId, projectId);

    ChatMessage chat = ChatMessage.builder()
                           .conversationId(conversation.getId())
                           .senderId(sender.getId())
                           .message(message)
                           .createdAt(LocalDateTime.now())
                           .build();

    return chatRepository.save(chat);
  }
}