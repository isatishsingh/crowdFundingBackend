package com.crowdfunding_backend.service;

import com.crowdfunding_backend.dto.creator.*;
import com.crowdfunding_backend.entity.*;
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

    profile.setAadhaarNumber(request.getAadhaarNumber());
    profile.setPanNumber(request.getPanNumber());
    profile.setGstNumber(request.getGstNumber());
    profile.setPassportNumber(request.getPassportNumber());
    profile.setPhoneNumber(request.getPhoneNumber());
    profile.setUser(user);

    if (profile.getAadhaarNumber() != null &&
        !profile.getAadhaarNumber().isEmpty() &&
        profile.getPanNumber() != null && !profile.getPanNumber().isEmpty()) {

      profile.setIsKycVerified(true);
    } else {
      profile.setIsKycVerified(false);
    }

    creatorProfileRepository.save(profile);

    return new CreatorProfileResponse(user.getEmail(),
                                      profile.getIsKycVerified());
  }
}