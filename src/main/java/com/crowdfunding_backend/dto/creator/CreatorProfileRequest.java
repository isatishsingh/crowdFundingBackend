package com.crowdfunding_backend.dto.creator;
import jakarta.validation.constraints.*;

public class CreatorProfileRequest {

  @NotBlank(message = "Aadhaar is required")
  @Pattern(regexp = "^\\d{12}$", message = "Aadhaar must be 12 digits")
  private String aadhaarNumber;

  @NotBlank(message = "PAN is required")
  @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$",
           message = "Invalid PAN format")
  private String panNumber;

  @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$",
           message = "Invalid GST number")

  private String gstNumber;

  @Pattern(regexp = "^[A-Z][0-9]{7}$", message = "Invalid passport number")
  private String passportNumber;

  @NotBlank(message = "Phone number is required")
  @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian phone number")
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