package com.crowdfunding_backend.controller;

import com.crowdfunding_backend.dto.chat.ChatMessageDTO;
import com.crowdfunding_backend.entity.ChatMessage;
import com.crowdfunding_backend.entity.User;
import com.crowdfunding_backend.repository.UserRepository;
import com.crowdfunding_backend.service.ChatService;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

  @Autowired private ChatService chatService;

  @Autowired private SimpMessagingTemplate messagingTemplate;

  @Autowired private UserRepository userRepository;

  @MessageMapping("/chat.send")
  public void sendMessage(@Payload ChatMessageDTO dto, Principal principal) {

    if (principal == null) {
      throw new RuntimeException("Unauthorized WebSocket request");
    }

    String senderEmail = principal.getName(); // SECURE SOURCE

    try {

      User receiver =
          userRepository.findById(dto.getReceiverId())
              .orElseThrow(() -> new RuntimeException("Receiver not found"));

      String receiverEmail = receiver.getEmail();

      ChatMessage savedMessage =
          chatService.sendMessage(senderEmail, dto.getReceiverId(),
                                  dto.getProjectId(), dto.getMessage());

      // SEND TO RECEIVER (PRIVATE)
      messagingTemplate.convertAndSendToUser(receiverEmail, "/queue/messages",
                                             savedMessage);

      // SEND BACK TO SENDER
      messagingTemplate.convertAndSendToUser(senderEmail, "/queue/messages",
                                             savedMessage);

    } catch (Exception e) {
      // SEND BACK TO SENDER ERROR
      messagingTemplate.convertAndSendToUser(senderEmail, "/queue/errors",
                                             e.getMessage());
    }
  }
}