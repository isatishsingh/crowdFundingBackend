package com.crowdfunding_backend.dto.creator;

import java.time.LocalDateTime;

public class CreatorKycAdminResponse {

  private Long profileId;
  private Long userId;
  private String creatorName;
  private String email;
  private String phoneNumber;
  private String panNumber;
  private String aadhaarNumber;
  private String gstNumber;
  private String passportNumber;
  private String kycStatus;
  private LocalDateTime submittedAt;

  public Long getProfileId() { return profileId; }

  public Long getUserId() { return userId; }

  public String getCreatorName() { return creatorName; }

  public String getEmail() { return email; }

  public String getPhoneNumber() { return phoneNumber; }

  public String getPanNumber() { return panNumber; }

  public String getAadhaarNumber() { return aadhaarNumber; }

  public String getGstNumber() { return gstNumber; }

  public String getPassportNumber() { return passportNumber; }

  public String getKycStatus() { return kycStatus; }

  public LocalDateTime getSubmittedAt() { return submittedAt; }

  public void setProfileId(Long profileId) { this.profileId = profileId; }

  public void setUserId(Long userId) { this.userId = userId; }

  public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

  public void setEmail(String email) { this.email = email; }

  public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

  public void setPanNumber(String panNumber) { this.panNumber = panNumber; }

  public void setAadhaarNumber(String aadhaarNumber) {
    this.aadhaarNumber = aadhaarNumber;
  }

  public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }

  public void setPassportNumber(String passportNumber) {
    this.passportNumber = passportNumber;
  }

  public void setKycStatus(String kycStatus) { this.kycStatus = kycStatus; }

  public void setSubmittedAt(LocalDateTime submittedAt) {
    this.submittedAt = submittedAt;
  }
}
