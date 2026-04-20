package com.crowdfunding_backend.controller;

import com.crowdfunding_backend.dto.chat.ChatMessageDTO;
import com.crowdfunding_backend.dto.chat.ConversationSummaryDTO;
import com.crowdfunding_backend.entity.ChatMessage;
import com.crowdfunding_backend.entity.User;
import com.crowdfunding_backend.service.ChatService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

  @Autowired private ChatService chatService;

  @PostMapping("/send")
  public ChatMessage sendMessage(Authentication authentication,
                                 @RequestBody ChatMessageDTO dto) {

    User user = (User)authentication.getPrincipal(); // ✅ FIX

    String email = user.getEmail(); // ✅ correct

    return chatService.sendMessage(email, dto.getReceiverId(),
                                   dto.getProjectId(), dto.getMessage());
  }

  @GetMapping("/messages")
  public List<ChatMessage> getChatHistory(Authentication authentication,
                                          @RequestParam Long receiverId,
                                          @RequestParam Long projectId) {

    User user = (User)authentication.getPrincipal();

    return chatService.getChatHistory(user.getEmail(), receiverId, projectId);
  }

  @GetMapping("/messages/by-conversation")
  public List<ChatMessage> getChatHistoryByConversation(
      Authentication authentication,
      @RequestParam Long conversationId) {
    User user = (User) authentication.getPrincipal();
    return chatService.getChatHistoryByConversationId(user.getEmail(),
                                                      conversationId);
  }

  @GetMapping("/conversations")
  public List<ConversationSummaryDTO>
  getConversations(Authentication authentication) {
    User user = (User) authentication.getPrincipal();
    return chatService.getConversations(user.getEmail());
  }
}