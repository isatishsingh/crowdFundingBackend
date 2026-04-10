package com.crowdfunding_backend.config;

import com.crowdfunding_backend.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;


@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

  @Autowired private JwtUtil jwtUtil;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {

    StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor != null &&
        StompCommand.CONNECT.equals(accessor.getCommand())) {

      System.out.println("🔥 CONNECT ATTEMPT");

      // 🔥 Get ALL headers
      Map<String, List<String>> headers = accessor.toNativeHeaderMap();
      System.out.println("HEADERS: " + headers);

      String token = null;

      // ✅ Try both cases
      if (headers.containsKey("Authorization")) {
        token = headers.get("Authorization").get(0);
      } else if (headers.containsKey("authorization")) {
        token = headers.get("authorization").get(0);
      }

      System.out.println("TOKEN: " + token);

      if (token != null && token.startsWith("Bearer ")) {
        token = token.substring(7);

        try {
          Claims claims = Jwts.parserBuilder()
                              .setSigningKey(jwtUtil.getSigningKey())
                              .build()
                              .parseClaimsJws(token)
                              .getBody();

          String email = claims.getSubject();

          System.out.println("✅ AUTH USER: " + email);

          accessor.setUser(() -> email);

        } catch (Exception e) {
          System.out.println("❌ JWT ERROR: " + e.getMessage());
        }
      } else {
        System.out.println("❌ TOKEN NOT FOUND");
      }
    }

    return message;
  }
}