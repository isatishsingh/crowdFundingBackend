package com.crowdfunding_backend.service;

import com.crowdfunding_backend.dto.chat.ConversationSummaryDTO;
import com.crowdfunding_backend.entity.*;
import com.crowdfunding_backend.repository.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

  public List<ChatMessage> getChatHistory(String email, Long receiverId,
                                          Long projectId) {

    User sender = userRepository.findByEmailIgnoreCase(email).orElseThrow(
        () -> new RuntimeException("User not found"));

    Long smaller = Math.min(sender.getId(), receiverId);
    Long larger = Math.max(sender.getId(), receiverId);

    // IMPORTANT: viewing chat history must NOT create a conversation
    return conversationRepository
        .findByUser1IdAndUser2IdAndProjectId(smaller, larger, projectId)
        .map(c -> chatRepository.findByConversationIdOrderByCreatedAtAsc(c.getId()))
        .orElseGet(List::of);
  }

  public List<ConversationSummaryDTO> getConversations(String email) {
    User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(
        () -> new RuntimeException("User not found"));

    List<Conversation> conversations =
        conversationRepository.findByUser1IdOrUser2IdOrderByCreatedAtDesc(
            user.getId(), user.getId());

    // Build summaries and drop empty conversations (no messages),
    // then de-duplicate by (minUser,maxUser,projectId) for legacy data.
    Map<String, ConversationSummaryDTO> bestByKey = new HashMap<>();

    for (Conversation conversation : conversations) {
      ChatMessage lastMessage =
          chatRepository.findTopByConversationIdOrderByCreatedAtDesc(
              conversation.getId());

      // If a conversation has no messages, don't show it in the list.
      if (lastMessage == null) {
        continue;
      }

      Long otherUserId = conversation.getUser1Id().equals(user.getId())
          ? conversation.getUser2Id()
          : conversation.getUser1Id();

      User otherUser = userRepository.findById(otherUserId).orElse(null);
      Project project =
          projectRepository.findById(conversation.getProjectId()).orElse(null);

      ConversationSummaryDTO dto = ConversationSummaryDTO.builder()
          .conversationId(conversation.getId())
          .projectId(conversation.getProjectId())
          .projectTitle(project != null ? project.getTitle() : "Project")
          .receiverId(otherUserId)
          .receiverName(otherUser != null ? otherUser.getName() : "Participant")
          .lastMessage(lastMessage.getMessage())
          .lastMessageAt(lastMessage.getCreatedAt())
          .build();

      Long smaller = Math.min(conversation.getUser1Id(), conversation.getUser2Id());
      Long larger = Math.max(conversation.getUser1Id(), conversation.getUser2Id());
      String key = smaller + ":" + larger + ":" + conversation.getProjectId();

      ConversationSummaryDTO existing = bestByKey.get(key);
      if (existing == null) {
        bestByKey.put(key, dto);
        continue;
      }

      LocalDateTime existingAt = existing.getLastMessageAt();
      if (existingAt == null || dto.getLastMessageAt().isAfter(existingAt)) {
        bestByKey.put(key, dto);
      }
    }

    List<ConversationSummaryDTO> result = new ArrayList<>(bestByKey.values());
    result.sort(Comparator.comparing(ConversationSummaryDTO::getLastMessageAt,
                                     Comparator.nullsLast(Comparator.naturalOrder()))
                    .reversed());
    return result;
  }

  public List<ChatMessage> getChatHistoryByConversationId(String email,
                                                          Long conversationId) {
    User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(
        () -> new RuntimeException("User not found"));

    Conversation conversation = conversationRepository.findById(conversationId)
        .orElseThrow(() -> new RuntimeException("Conversation not found"));

    if (!Objects.equals(conversation.getUser1Id(), user.getId()) &&
        !Objects.equals(conversation.getUser2Id(), user.getId())) {
      throw new RuntimeException("Not allowed to view this conversation");
    }

    return chatRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
  }
}