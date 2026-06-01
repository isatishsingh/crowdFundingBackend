package com.crowdfunding_backend.service;

import com.crowdfunding_backend.dto.user.UpdateUserProfileRequest;
import com.crowdfunding_backend.dto.user.UserProfileResponse;
import com.crowdfunding_backend.entity.CreatorProfile;
import com.crowdfunding_backend.entity.KycStatus;
import com.crowdfunding_backend.entity.Role;
import com.crowdfunding_backend.entity.User;
import com.crowdfunding_backend.exception.CustomException;
import com.crowdfunding_backend.repository.CreatorProfileRepository;
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

  @Autowired private CreatorProfileRepository creatorProfileRepository;

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
      user.setRole(Role.INVESTOR); // default role
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

  public UserProfileResponse getProfile(String email) {
    User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(
        () -> new CustomException("User account not found.", 404));

    Boolean kycVerified = null;
    String kycStatus = null;
    if (user.getRole() == Role.CREATOR) {
      CreatorProfile profile =
          creatorProfileRepository.findByUser_Id(user.getId()).orElse(null);
      if (profile != null) {
        kycStatus =
            profile.getKycStatus() != null ? profile.getKycStatus().name() : null;
        kycVerified = profile.getKycStatus() == KycStatus.APPROVED &&
                        Boolean.TRUE.equals(profile.getIsKycVerified());
      } else {
        kycVerified = false;
        kycStatus = KycStatus.NOT_SUBMITTED.name();
      }
    }

    return new UserProfileResponse(
        user.getId(), user.getName(), user.getEmail(), user.getRole().name(),
        user.isCreatorMembershipActive(), user.isInvestorMembershipActive(),
        kycVerified, kycStatus);
  }

  public UserProfileResponse updateProfile(String email,
                                           UpdateUserProfileRequest request) {
    User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(
        () -> new CustomException("User account not found.", 404));

    user.setName(request.getName().trim());

    String newPassword = request.getNewPassword();
    if (newPassword != null && !newPassword.isBlank()) {
      String currentPassword = request.getCurrentPassword();
      if (currentPassword == null || currentPassword.isBlank()) {
        throw new CustomException(
            "Enter your current password to set a new password.",
            400, "PASSWORD_CURRENT_REQUIRED");
      }
      if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
        throw new CustomException(
            "Current password is incorrect. Your password was not changed.",
            400, "PASSWORD_CURRENT_INVALID");
      }
      user.setPassword(passwordEncoder.encode(newPassword.trim()));
    }

    userRepository.save(user);
    return getProfile(email);
  }
}