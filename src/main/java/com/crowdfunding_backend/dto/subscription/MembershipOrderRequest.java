package com.crowdfunding_backend.dto.subscription;

import com.crowdfunding_backend.entity.MembershipPlan;
import lombok.Data;

@Data
public class MembershipOrderRequest {
  private MembershipPlan plan;
}
