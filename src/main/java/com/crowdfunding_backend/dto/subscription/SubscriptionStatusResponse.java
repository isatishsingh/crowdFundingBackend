package com.crowdfunding_backend.dto.subscription;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubscriptionStatusResponse {

  private String role;
  private boolean hasMembership;
  private int freeProjectLimit;
  private int projectsCreated;
  private double freeGoalLimitInr;
  private double freeInvestmentLimitInr;
  private int freeProjectInvestLimit;
  private int distinctProjectsUsed;
  private double membershipPriceInr;
  private boolean canCreateProject;
  private boolean canInvest;
  private String guidanceMessage;
}
