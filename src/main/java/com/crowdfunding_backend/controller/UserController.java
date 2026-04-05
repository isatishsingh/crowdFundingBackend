package com.crowdfunding_backend.controller;

import com.crowdfunding_backend.dto.AuthResponse;
import com.crowdfunding_backend.entity.User;
import com.crowdfunding_backend.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

  @Autowired private UserService userService;

  // Create User (Register)
  @PostMapping("/register")
  public User createUser(@Valid @RequestBody User user) {
    return userService.saveUser(user);
  }

  @PostMapping("/login")
  public AuthResponse login(@RequestBody User user) {
    String token =
        userService.loginAndGenerateToken(user.getEmail(), user.getPassword());

    return new AuthResponse(token);
  }

  @PreAuthorize("hasRole('CREATOR')")
  @GetMapping("/creator/dashboard")
  public String creatorDashboard() {
    return "Welcome Creator Dashboard 🚀";
  }

  @PreAuthorize("hasRole('INVESTOR')")
  @GetMapping("/investor/dashboard")
  public String investorDashboard() {
    return "Welcome Investor Dashboard 💰";
  }

  // Get All Users
  @GetMapping
  public List<User> getAllUsers() {
    return userService.getAllUsers();
  }

  // Get User by ID
  @GetMapping("/{id}")
  public User getUserById(@PathVariable Long id) {
    return userService.getUserById(id);
  }

  // Delete User
  @DeleteMapping("/{id}")
  public String deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
    return "User deleted successfully";
  }
}