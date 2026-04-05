package com.crowdfunding_backend.dto.creator;

public class CreatorProfileRequest {

  private String aadhaarNumber;
  private String panNumber;
  private String gstNumber;
  private String passportNumber;
  private String phoneNumber;

  // ✅ GETTERS

  public String getAadhaarNumber() { return aadhaarNumber; }

  public String getPanNumber() { return panNumber; }

  public String getGstNumber() { return gstNumber; }

  public String getPassportNumber() { return passportNumber; }

  public String getPhoneNumber() { return phoneNumber; }

  // ✅ SETTERS

  public void setAadhaarNumber(String aadhaarNumber) {
    this.aadhaarNumber = aadhaarNumber;
  }

  public void setPanNumber(String panNumber) { this.panNumber = panNumber; }

  public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }

  public void setPassportNumber(String passportNumber) {
    this.passportNumber = passportNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }
}