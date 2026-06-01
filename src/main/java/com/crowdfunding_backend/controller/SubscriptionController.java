package com.crowdfunding_backend.controller;

import com.crowdfunding_backend.dto.payment.CreateOrderResponse;
import com.crowdfunding_backend.dto.payment.VerifyPaymentRequest;
import com.crowdfunding_backend.dto.payment.VerifyPaymentResponse;
import com.crowdfunding_backend.dto.subscription.MembershipOrderRequest;
import com.crowdfunding_backend.dto.subscription.SubscriptionStatusResponse;
import com.crowdfunding_backend.entity.User;
import com.crowdfunding_backend.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

  @Autowired private SubscriptionService subscriptionService;

  @GetMapping("/status")
  public SubscriptionStatusResponse getStatus(Authentication authentication) {
    User user = (User)authentication.getPrincipal();
    return subscriptionService.getStatus(user);
  }

  @PostMapping("/membership-order")
  public CreateOrderResponse createMembershipOrder(
      Authentication authentication, @RequestBody MembershipOrderRequest request) {
    User user = (User)authentication.getPrincipal();
    return subscriptionService.createMembershipOrder(user, request.getPlan());
  }

  @PostMapping("/verify-membership")
  public VerifyPaymentResponse verifyMembership(
      Authentication authentication, @RequestBody VerifyPaymentRequest request) {
    User user = (User)authentication.getPrincipal();
    return subscriptionService.verifyMembershipPayment(user, request);
  }
}
