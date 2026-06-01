package com.crowdfunding_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class CreatorProfile {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

  private String aadhaarNumber;
  private String panNumber;
  private String gstNumber;
  private String passportNumber;
  private String phoneNumber;

  private Boolean isEmailVerified = false;
  private Boolean isMobileVerified = false;
  private Boolean isKycVerified = false;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private KycStatus kycStatus = KycStatus.NOT_SUBMITTED;

  private LocalDateTime kycSubmittedAt;

  @OneToOne @JoinColumn(name = "user_id") private User user;

  // getters and setters
  public Long getId() { return id; }

  public void setId(Long id) { this.id = id; }

  public String getAadhaarNumber() { return aadhaarNumber; }

  public void setAadhaarNumber(String aadhaarNumber) {
    this.aadhaarNumber = aadhaarNumber;
  }

  public String getPanNumber() { return panNumber; }

  public void setPanNumber(String panNumber) { this.panNumber = panNumber; }

  public String getGstNumber() { return gstNumber; }

  public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }

  public String getPassportNumber() { return passportNumber; }

  public void setPassportNumber(String passportNumber) {
    this.passportNumber = passportNumber;
  }

  public String getPhoneNumber() { return phoneNumber; }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public Boolean getIsKycVerified() { return isKycVerified; }

  public Boolean getIsEmailVerified() { return isEmailVerified; }

  public Boolean getIsMobileVerified() { return isMobileVerified; }

  public void setIsKycVerified(Boolean isKycVerified) {
    this.isKycVerified = isKycVerified;
  }

  public void setIsEmailVerified(Boolean isEmailVerified) {
    this.isEmailVerified = isEmailVerified;
  }

  public void setIsMobileVerified(Boolean isMobileVerified) {
    this.isMobileVerified = isMobileVerified;
  }

  public User getUser() { return user; }

  public void setUser(User user) { this.user = user; }

  public KycStatus getKycStatus() { return kycStatus; }

  public void setKycStatus(KycStatus kycStatus) { this.kycStatus = kycStatus; }

  public LocalDateTime getKycSubmittedAt() { return kycSubmittedAt; }

  public void setKycSubmittedAt(LocalDateTime kycSubmittedAt) {
    this.kycSubmittedAt = kycSubmittedAt;
  }
}