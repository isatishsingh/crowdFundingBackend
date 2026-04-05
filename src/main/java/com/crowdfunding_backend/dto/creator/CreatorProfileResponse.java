package com.crowdfunding_backend.dto.creator;

public class CreatorProfileResponse {

  private String email;
  private Boolean isKycVerified;

  // constructor
  public CreatorProfileResponse(String email, Boolean isKycVerified) {
    this.email = email;
    this.isKycVerified = isKycVerified;
  }

  public String getEmail() { return email; }

  public Boolean getIsKycVerified() { return isKycVerified; }
}