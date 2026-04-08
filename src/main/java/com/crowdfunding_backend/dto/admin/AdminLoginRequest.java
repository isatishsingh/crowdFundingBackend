package com.crowdfunding_backend.dto.admin;

public class AdminLoginRequest {
  private String email;
  private String password;

  // getters & setters
  public String getEmail() { return email; }

  // ✅ Setter for email
  public void setEmail(String email) { this.email = email; }

  // ✅ Getter for password
  public String getPassword() { return password; }

  // ✅ Setter for password
  public void setPassword(String password) { this.password = password; }
}
