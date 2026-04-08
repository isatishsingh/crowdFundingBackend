package com.crowdfunding_backend.dto.admin;

public class AdminDashboardResponse {

  private long totalUsers;
  private long totalProjects;
  private long totalInvestments;
  private double totalFunding;

  // getters & setters
  public long getTotalUsers() { return totalUsers; }
  public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

  public long getTotalProjects() { return totalProjects; }
  public void setTotalProjects(long totalProjects) {
    this.totalProjects = totalProjects;
  }

  public long getTotalInvestments() { return totalInvestments; }
  public void setTotalInvestments(long totalInvestments) {
    this.totalInvestments = totalInvestments;
  }

  public double getTotalFunding() { return totalFunding; }
  public void setTotalFunding(double totalFunding) {
    this.totalFunding = totalFunding;
  }
}