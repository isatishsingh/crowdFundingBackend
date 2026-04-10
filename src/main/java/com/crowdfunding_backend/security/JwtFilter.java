package com.crowdfunding_backend.security;

import com.crowdfunding_backend.entity.User;
import com.crowdfunding_backend.repository.UserRepository;
import com.crowdfunding_backend.util.AdminJwtUtil;
import com.crowdfunding_backend.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtFilter extends OncePerRequestFilter {

  @Autowired private JwtUtil jwtUtil;

  @Autowired private AdminJwtUtil adminJwtUtil;

  @Autowired private UserRepository userRepository;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain)
      throws ServletException, IOException {

    String path = request.getRequestURI();

    if (path.startsWith("/ws-chat")) {
      filterChain.doFilter(request, response);
      return;
    }

    final String authHeader = request.getHeader("Authorization");

    String email = null;
    String token = null;

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      token = authHeader.substring(7);

      try {
        // 🔥 STEP 1: Try ADMIN token
        Claims claims =
            Jwts.parserBuilder()
                .setSigningKey(adminJwtUtil.getSigningKey()) // ADMIN KEY
                .build()
                .parseClaimsJws(token)
                .getBody();

        email = claims.getSubject();

      } catch (Exception e) {

        try {
          // 🔥 STEP 2: Try USER token
          Claims claims =
              Jwts.parserBuilder()
                  .setSigningKey(jwtUtil.getSigningKey()) // USER KEY
                  .build()
                  .parseClaimsJws(token)
                  .getBody();

          email = claims.getSubject();

        } catch (Exception ex) {
          // invalid token
        }
      }
    }

    // 🔐 STEP 3: Set authentication
    if (email != null &&
        SecurityContextHolder.getContext().getAuthentication() == null) {

      User user = userRepository.findByEmailIgnoreCase(email).orElse(null);

      if (user != null) {

        List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(
                user, null, authorities); // roles can be added later

        authToken.setDetails(
            new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authToken);
      }
    }

    filterChain.doFilter(request, response);
  }
}