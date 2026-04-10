package com.crowdfunding_backend.controller;

import com.crowdfunding_backend.dto.chat.ChatMessageDTO;
import com.crowdfunding_backend.entity.ChatMessage;
import com.crowdfunding_backend.entity.User;
import com.crowdfunding_backend.service.ChatService;
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
}