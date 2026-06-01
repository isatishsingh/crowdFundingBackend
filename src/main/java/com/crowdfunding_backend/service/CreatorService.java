package com.crowdfunding_backend.service;

import com.crowdfunding_backend.dto.creator.*;
import com.crowdfunding_backend.entity.*;
import com.crowdfunding_backend.exception.CustomException;
import com.crowdfunding_backend.repository.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
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

    if (profile.getKycStatus() == KycStatus.APPROVED &&
        Boolean.TRUE.equals(profile.getIsKycVerified())) {

      return buildResponse(user, profile,
                           "Your profile is already verified by an admin.");
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

    if (hasRequiredKycFields(profile)) {
      profile.setKycStatus(KycStatus.PENDING);
      profile.setIsKycVerified(false);
      profile.setIsEmailVerified(false);
      profile.setIsMobileVerified(false);
      profile.setKycSubmittedAt(LocalDateTime.now());
    } else {
      profile.setKycStatus(KycStatus.NOT_SUBMITTED);
      profile.setIsKycVerified(false);
    }

    creatorProfileRepository.save(profile);

    return buildResponse(
        user, profile,
        "Verification submitted successfully. An admin will review your application. "
            + "You can create projects after approval.");
  }

  public CreatorProfileResponse getProfileStatus(String email) {
    User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(
        () -> new CustomException("User account not found.", 404));

    CreatorProfile profile =
        creatorProfileRepository.findByUser_Id(user.getId()).orElse(null);

    if (profile == null) {
      return new CreatorProfileResponse(user.getEmail(), false,
                                        KycStatus.NOT_SUBMITTED.name(),
                                        "You have not applied for verification yet.");
    }

    return buildResponse(user, profile, statusMessage(profile));
  }

  public List<CreatorKycAdminResponse> listProfilesForAdmin(KycStatus status) {
    KycStatus filter = status != null ? status : KycStatus.PENDING;
    return creatorProfileRepository.findByKycStatusOrderByKycSubmittedAtDesc(filter)
        .stream()
        .map(this::toAdminResponse)
        .collect(Collectors.toList());
  }

  public CreatorKycAdminResponse approveProfile(Long profileId) {
    CreatorProfile profile = loadProfile(profileId);
    if (profile.getKycStatus() != KycStatus.PENDING) {
      throw new CustomException(
          "Only pending verification requests can be approved.", 400);
    }
    if (!hasRequiredKycFields(profile)) {
      throw new CustomException(
          "This profile is missing required verification details.", 400);
    }

    profile.setKycStatus(KycStatus.APPROVED);
    profile.setIsKycVerified(true);
    profile.setIsEmailVerified(true);
    profile.setIsMobileVerified(true);
    creatorProfileRepository.save(profile);

    return toAdminResponse(profile);
  }

  public CreatorKycAdminResponse rejectProfile(Long profileId) {
    CreatorProfile profile = loadProfile(profileId);
    if (profile.getKycStatus() != KycStatus.PENDING) {
      throw new CustomException(
          "Only pending verification requests can be rejected.", 400);
    }

    profile.setKycStatus(KycStatus.REJECTED);
    profile.setIsKycVerified(false);
    profile.setIsEmailVerified(false);
    profile.setIsMobileVerified(false);
    creatorProfileRepository.save(profile);

    return toAdminResponse(profile);
  }

  public static void assertCreatorMayCreateProjects(CreatorProfile profile) {
    if (profile == null || !hasRequiredKycFields(profile)) {
      throw new CustomException(
          "Complete creator verification (KYC) before listing a project.",
          403, "KYC_NOT_SUBMITTED");
    }

    KycStatus status = profile.getKycStatus();
    if (status == null) {
      status = Boolean.TRUE.equals(profile.getIsKycVerified()) ? KycStatus.APPROVED
                                                               : KycStatus.NOT_SUBMITTED;
    }

    if (status == KycStatus.PENDING) {
      throw new CustomException(
          "Your verification is pending admin approval. Project creation will be "
              + "available once an admin approves your profile.",
          403, "KYC_PENDING");
    }

    if (status == KycStatus.REJECTED) {
      throw new CustomException(
          "Your verification was not approved. Please update your details and apply again "
              + "from the verification page.",
          403, "KYC_REJECTED");
    }

    if (status != KycStatus.APPROVED ||
        !Boolean.TRUE.equals(profile.getIsKycVerified())) {
      throw new CustomException(
          "Complete creator verification (KYC) before listing a project.",
          403, "KYC_NOT_SUBMITTED");
    }
  }

  private CreatorProfile loadProfile(Long profileId) {
    return creatorProfileRepository.findById(profileId).orElseThrow(
        () -> new CustomException("Verification profile not found.", 404));
  }

  private CreatorKycAdminResponse toAdminResponse(CreatorProfile profile) {
    CreatorKycAdminResponse dto = new CreatorKycAdminResponse();
    User user = profile.getUser();
    dto.setProfileId(profile.getId());
    dto.setUserId(user != null ? user.getId() : null);
    dto.setCreatorName(user != null ? user.getName() : null);
    dto.setEmail(user != null ? user.getEmail() : null);
    dto.setPhoneNumber(profile.getPhoneNumber());
    dto.setPanNumber(profile.getPanNumber());
    dto.setAadhaarNumber(profile.getAadhaarNumber());
    dto.setGstNumber(profile.getGstNumber());
    dto.setPassportNumber(profile.getPassportNumber());
    dto.setKycStatus(
        profile.getKycStatus() != null ? profile.getKycStatus().name() : null);
    dto.setSubmittedAt(profile.getKycSubmittedAt());
    return dto;
  }

  private CreatorProfileResponse buildResponse(User user, CreatorProfile profile,
                                             String message) {
    return new CreatorProfileResponse(
        user.getEmail(), profile.getIsKycVerified(),
        profile.getKycStatus() != null ? profile.getKycStatus().name()
                                       : KycStatus.NOT_SUBMITTED.name(),
        message);
  }

  private String statusMessage(CreatorProfile profile) {
    if (profile.getKycStatus() == KycStatus.APPROVED) {
      return "Your profile is verified. You can create projects.";
    }
    if (profile.getKycStatus() == KycStatus.PENDING) {
      return "Your verification is pending admin approval.";
    }
    if (profile.getKycStatus() == KycStatus.REJECTED) {
      return "Your verification was rejected. Please submit updated details.";
    }
    return "Submit your verification details to apply.";
  }

  private static boolean hasRequiredKycFields(CreatorProfile profile) {
    return profile.getAadhaarNumber() != null &&
        !profile.getAadhaarNumber().isBlank() && profile.getPanNumber() != null &&
        !profile.getPanNumber().isBlank() && profile.getPhoneNumber() != null &&
        !profile.getPhoneNumber().isBlank();
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
