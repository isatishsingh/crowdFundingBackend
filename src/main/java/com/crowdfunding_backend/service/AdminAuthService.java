package com.crowdfunding_backend.service;

import com.crowdfunding_backend.dto.admin.AdminLoginRequest;
import com.crowdfunding_backend.entity.User;
import com.crowdfunding_backend.repository.UserRepository;
import com.crowdfunding_backend.util.AdminJwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {

  @Autowired private UserRepository userRepository;

  @Autowired private AdminJwtUtil adminJwtUtil;
  @Autowired private PasswordEncoder passwordEncoder;

  public String login(AdminLoginRequest request) {

    User admin =
        userRepository.findByEmailIgnoreCase(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Admin not found"));

    if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
      throw new RuntimeException("Invalid password");
    }

    if (!admin.getRole().name().equals("ADMIN")) {
      throw new RuntimeException("Not an admin");
    }

    return adminJwtUtil.generateToken(admin.getEmail());
  }
}