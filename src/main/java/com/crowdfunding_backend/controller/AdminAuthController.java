package com.crowdfunding_backend.controller;

import com.crowdfunding_backend.dto.AuthResponse;
import com.crowdfunding_backend.dto.admin.AdminLoginRequest;
import com.crowdfunding_backend.service.AdminAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminAuthController {

  @Autowired private AdminAuthService adminAuthService;

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody AdminLoginRequest request) {

    String token = adminAuthService.login(request);

    return ResponseEntity.ok(new AuthResponse(token));
  }
}