package com.crowdfunding_backend.dto.user;

public class UserProfileResponse {

  private Long id;
  private String name;
  private String email;
  private String role;
  private boolean creatorMembershipActive;
  private boolean investorMembershipActive;
  private Boolean kycVerified;

  public UserProfileResponse() {}

  public UserProfileResponse(Long id, String name, String email, String role,
                             boolean creatorMembershipActive,
                             boolean investorMembershipActive,
                             Boolean kycVerified) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.role = role;
    this.creatorMembershipActive = creatorMembershipActive;
    this.investorMembershipActive = investorMembershipActive;
    this.kycVerified = kycVerified;
  }

  public Long getId() { return id; }

  public String getName() { return name; }

  public String getEmail() { return email; }

  public String getRole() { return role; }

  public boolean isCreatorMembershipActive() { return creatorMembershipActive; }

  public boolean isInvestorMembershipActive() { return investorMembershipActive; }

  public Boolean getKycVerified() { return kycVerified; }
}
