package com.crowdfunding_backend.dto.creator;

public class CreatorProfileResponse {

  private String email;
  private Boolean isKycVerified;
  private String kycStatus;
  private String message;

  // constructor
  public CreatorProfileResponse(String email, Boolean isKycVerified) {
    this.email = email;
    this.isKycVerified = isKycVerified;
  }

  public CreatorProfileResponse(String email, Boolean isKycVerified, String kycStatus,
                                String message) {
    this.email = email;
    this.isKycVerified = isKycVerified;
    this.kycStatus = kycStatus;
    this.message = message;
  }

  public String getEmail() { return email; }

  public Boolean getIsKycVerified() { return isKycVerified; }

  public String getKycStatus() { return kycStatus; }

  public String getMessage() { return message; }
}