package com.crowdfunding_backend.service;

import com.crowdfunding_backend.dto.creator.*;
import com.crowdfunding_backend.entity.*;
import com.crowdfunding_backend.exception.CustomException;
import com.crowdfunding_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreatorService {

  @Autowired private CreatorProfileRepository creatorProfileRepository;

  @Autowired private UserRepository userRepository;

  public CreatorProfileResponse
  createOrUpdateProfile(CreatorProfileRequest request, String email) {

    User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(
        () -> new RuntimeException("User not found"));

    CreatorProfile profile =
        creatorProfileRepository.findByUser_Id(user.getId())
            .orElse(new CreatorProfile());

    if (Boolean.TRUE.equals(profile.getIsKycVerified()) &&
        Boolean.TRUE.equals(profile.getIsMobileVerified()) &&
        Boolean.TRUE.equals(profile.getIsEmailVerified())) {

      return new CreatorProfileResponse(user.getEmail(), true,
                                        "Profile is already verified");
    }

    String phoneNumber = request.getPhoneNumber().trim();
    String panNumber = request.getPanNumber().trim().toUpperCase();
    String aadhaarNumber = request.getAadhaarNumber().trim();

    assertUniqueKycIdentifiers(user.getId(), phoneNumber, panNumber, aadhaarNumber);

    profile.setAadhaarNumber(aadhaarNumber);
    profile.setPanNumber(panNumber);
    profile.setGstNumber(normalizeOptional(request.getGstNumber(), true));
    profile.setPassportNumber(normalizeOptional(request.getPassportNumber(), true));
    profile.setPhoneNumber(phoneNumber);
    profile.setUser(user);

    if (profile.getAadhaarNumber() != null &&
        !profile.getAadhaarNumber().isEmpty() &&
        profile.getPanNumber() != null && !profile.getPanNumber().isEmpty()) {

      profile.setIsKycVerified(true);
      profile.setIsEmailVerified(true);
      profile.setIsMobileVerified(true);
    } else {
      profile.setIsKycVerified(false);
    }

    creatorProfileRepository.save(profile);

    return new CreatorProfileResponse(user.getEmail(),
                                      profile.getIsKycVerified());
  }

  private String normalizeOptional(String value, boolean uppercase) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    if (trimmed.isEmpty()) {
      return null;
    }
    return uppercase ? trimmed.toUpperCase() : trimmed;
  }

  private void assertUniqueKycIdentifiers(Long userId, String phoneNumber,
                                        String panNumber, String aadhaarNumber) {
    if (creatorProfileRepository.existsByPhoneNumberAndUser_IdNot(phoneNumber,
                                                                  userId)) {
      throw new CustomException(
          "This phone number is already registered with another account. "
              + "Please use a different phone number.",
          400, "KYC_PHONE_IN_USE");
    }

    if (creatorProfileRepository.existsByPanNumberIgnoreCaseAndUser_IdNot(
            panNumber, userId)) {
      throw new CustomException(
          "This PAN number is already registered with another account. "
              + "Please enter your own PAN.",
          400, "KYC_PAN_IN_USE");
    }

    if (creatorProfileRepository.existsByAadhaarNumberAndUser_IdNot(
            aadhaarNumber, userId)) {
      throw new CustomException(
          "This Aadhaar number is already registered with another account. "
              + "Please enter your own Aadhaar number.",
          400, "KYC_AADHAAR_IN_USE");
    }
  }
}