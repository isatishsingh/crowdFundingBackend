package com.crowdfunding_backend.exception;

import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.ControllerAdvice;


@ControllerAdvice
public class WebSocketExceptionHandler {

  @Autowired private SimpMessagingTemplate messagingTemplate;

  @MessageExceptionHandler
  public void handleException(Exception ex, Principal principal) {

    if (principal != null) {
      String user = principal.getName();

      System.out.println("🔥 ERROR: " + ex.getMessage());

      messagingTemplate.convertAndSendToUser(user, "/queue/errors",
                                             ex.getMessage());
    }
  }
}