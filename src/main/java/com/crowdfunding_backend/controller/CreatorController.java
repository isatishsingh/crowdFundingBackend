package com.crowdfunding_backend.controller;

import com.crowdfunding_backend.dto.creator.CreatorProfileRequest;
import com.crowdfunding_backend.dto.creator.CreatorProfileResponse;
import com.crowdfunding_backend.entity.User;
import com.crowdfunding_backend.service.CreatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/creator")
public class CreatorController {

  @Autowired private CreatorService creatorService;

  @PostMapping("/profile")
  @PreAuthorize("hasRole('CREATOR')")
  public CreatorProfileResponse
  createProfile(@RequestBody CreatorProfileRequest request,
                Authentication authentication) {
    Object principal = authentication.getPrincipal();
    System.out.println("Principal: " + principal);
    String email = principal instanceof User ? ((User)principal).getEmail()
                                             : authentication.getName();
    System.out.println("Authenticated user email: " + email);
    return creatorService.createOrUpdateProfile(request, email);
  }
}