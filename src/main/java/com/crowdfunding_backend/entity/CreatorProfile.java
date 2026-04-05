package com.crowdfunding_backend.entity;

import jakarta.persistence.*;

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

  @OneToOne @JoinColumn(name = "user_id") private User user;

  // getters and setters
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

  public void setIsKycVerified(Boolean isKycVerified) {
    this.isKycVerified = isKycVerified;
  }

  public User getUser() { return user; }

  public void setUser(User user) { this.user = user; }
}