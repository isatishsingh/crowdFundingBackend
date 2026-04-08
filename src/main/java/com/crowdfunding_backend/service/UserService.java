package com.crowdfunding_backend.service;

import com.crowdfunding_backend.entity.Role;
import com.crowdfunding_backend.entity.User;
import com.crowdfunding_backend.repository.UserRepository;
import com.crowdfunding_backend.util.JwtUtil;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  @Autowired private BCryptPasswordEncoder passwordEncoder;
  @Autowired private UserRepository userRepository;

  @Autowired private JwtUtil jwtUtil;

  public String loginAndGenerateToken(String email, String password) {
    User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(
        () -> new RuntimeException("User not found"));

    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new RuntimeException("Invalid password");
    }

    return jwtUtil.generateToken(email, user.getRole());
  }

  // Save User
  public User saveUser(User user) {
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    if (user.getRole() == null) {
      user.setRole(Role.ROLE_INVESTOR); // default role
    }
    return userRepository.save(user);
  }

  // Get All Users
  public List<User> getAllUsers() { return userRepository.findAll(); }

  // Get User by ID
  public User getUserById(Long id) {
    return userRepository.findById(id).orElse(null);
  }

  // Delete User
  public void deleteUser(Long id) { userRepository.deleteById(id); }
}