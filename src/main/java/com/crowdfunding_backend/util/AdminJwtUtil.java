package com.crowdfunding_backend.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AdminJwtUtil {

  @Value("${admin.jwt.secret}") private String SECRET_KEY;

  @Value("${admin.jwt.expiration}") private long expiration;

  public Key getSigningKey() {
    return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
  }

  public String generateToken(String email) {
    return Jwts.builder()
        .setSubject(email)
        .claim("role", "ROLE_ADMIN")
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getSigningKey()) // ✅ FIXED
        .compact();
  }
}