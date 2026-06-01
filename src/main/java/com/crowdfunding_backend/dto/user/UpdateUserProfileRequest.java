package com.crowdfunding_backend.dto.user;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateUserProfileRequest {

  @NotBlank(message = "Name is required")
  @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
  private String name;

  private String currentPassword;

  @Size(min = 6, message = "New password must be at least 6 characters")
  private String newPassword;

  public String getName() { return name; }

  public String getCurrentPassword() { return currentPassword; }

  public String getNewPassword() { return newPassword; }

  public void setName(String name) { this.name = name; }

  public void setCurrentPassword(String currentPassword) {
    this.currentPassword = currentPassword;
  }

  public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

  @AssertTrue(message = "Current password is required when setting a new password.")
  public boolean isPasswordChangeConsistent() {
    boolean hasNew = newPassword != null && !newPassword.isBlank();
    if (!hasNew) {
      return true;
    }
    return currentPassword != null && !currentPassword.isBlank();
  }
}
