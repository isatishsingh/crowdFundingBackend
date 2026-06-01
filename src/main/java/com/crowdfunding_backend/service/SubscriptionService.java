package com.crowdfunding_backend.service;

import com.crowdfunding_backend.config.RazorpayConfig;
import com.crowdfunding_backend.dto.payment.CreateOrderResponse;
import com.crowdfunding_backend.dto.payment.PaymentReceiptResponse;
import com.crowdfunding_backend.dto.payment.VerifyPaymentRequest;
import com.crowdfunding_backend.dto.payment.VerifyPaymentResponse;
import com.crowdfunding_backend.dto.subscription.SubscriptionStatusResponse;
import com.crowdfunding_backend.entity.*;
import com.crowdfunding_backend.exception.CustomException;
import com.crowdfunding_backend.repository.*;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionService {

  public static final String CODE_CREATOR_LIMIT = "CREATOR_SUBSCRIPTION_REQUIRED";
  public static final String CODE_INVESTOR_LIMIT = "INVESTOR_SUBSCRIPTION_REQUIRED";

  @Value("${subscription.free.max-projects:1}")
  private int freeMaxProjects;

  @Value("${subscription.free.max-goal-inr:10000}")
  private double freeMaxGoalInr;

  @Value("${subscription.free.max-investment-inr:10000}")
  private double freeMaxInvestmentInr;

  @Value("${subscription.free.max-investor-projects:1}")
  private int freeMaxInvestorProjects;

  @Value("${subscription.creator.membership-price-inr:999}")
  private double creatorMembershipPriceInr;

  @Value("${subscription.investor.membership-price-inr:499}")
  private double investorMembershipPriceInr;

  @Autowired private UserRepository userRepository;
  @Autowired private ProjectRepository projectRepository;
  @Autowired private InvestmentRepository investmentRepository;
  @Autowired private InvestmentRequestRepository investmentRequestRepository;
  @Autowired private PaymentRepository paymentRepository;
  @Autowired private RazorpayConfig razorpayConfig;

  @Value("${razorpay.secret}")
  private String razorpaySecret;

  public SubscriptionStatusResponse getStatus(User user) {
    if (user.getRole() == Role.CREATOR) {
      return buildCreatorStatus(user);
    }
    if (user.getRole() == Role.INVESTOR) {
      return buildInvestorStatus(user, null, null);
    }
    return SubscriptionStatusResponse.builder()
        .role(user.getRole().name())
        .hasMembership(true)
        .canCreateProject(true)
        .canInvest(true)
        .guidanceMessage("Admin accounts are not limited by subscription tiers.")
        .build();
  }

  public void validateCreatorCanCreateProject(User creator, Double goalAmount) {
    if (creator.isCreatorMembershipActive()) {
      return;
    }

    int projectCount = projectRepository.findByCreator_IdOrderByCreatedAtDesc(creator.getId()).size();
    if (projectCount >= freeMaxProjects) {
      throw new CustomException(
          "Free plan allows only "
              + freeMaxProjects
              + " listed project. Upgrade to Creator Membership to list more campaigns.",
          402,
          CODE_CREATOR_LIMIT);
    }

    validateCreatorGoalAmount(creator, goalAmount);
  }

  public void validateCreatorGoalAmount(User creator, Double goalAmount) {
    if (creator.isCreatorMembershipActive()) {
      return;
    }

    if (goalAmount != null && goalAmount > freeMaxGoalInr) {
      throw new CustomException(
          "Free plan supports fundraising goals up to ₹"
              + (long)freeMaxGoalInr
              + ". Upgrade to Creator Membership to raise more than that.",
          402,
          CODE_CREATOR_LIMIT);
    }
  }

  public void validateInvestorCanInvest(User investor, Long projectId, Double amount) {
    if (investor.isInvestorMembershipActive()) {
      return;
    }

    if (amount != null && amount > freeMaxInvestmentInr) {
      throw new CustomException(
          "Free plan allows investments up to ₹"
              + (long)freeMaxInvestmentInr
              + " per project. Upgrade to Investor Membership to invest larger amounts.",
          402,
          CODE_INVESTOR_LIMIT);
    }

    Set<Long> usedProjects = getInvestorProjectIds(investor.getId());
    boolean isExistingProject =
        projectId != null && usedProjects.contains(projectId);

    if (!isExistingProject && usedProjects.size() >= freeMaxInvestorProjects) {
      throw new CustomException(
          "Free plan allows investing in "
              + freeMaxInvestorProjects
              + " project only. Upgrade to Investor Membership to invest in more projects.",
          402,
          CODE_INVESTOR_LIMIT);
    }
  }

  public CreateOrderResponse createMembershipOrder(User user, MembershipPlan plan) {
  if (plan == MembershipPlan.CREATOR && user.getRole() != Role.CREATOR) {
      throw new CustomException("Creator membership is only for creator accounts.", 400);
    }
    if (plan == MembershipPlan.INVESTOR && user.getRole() != Role.INVESTOR) {
      throw new CustomException("Investor membership is only for investor accounts.", 400);
    }
    if (plan == MembershipPlan.CREATOR && user.isCreatorMembershipActive()) {
      throw new CustomException("Creator membership is already active on your account.", 400);
    }
    if (plan == MembershipPlan.INVESTOR && user.isInvestorMembershipActive()) {
      throw new CustomException("Investor membership is already active on your account.", 400);
    }

    double price =
        plan == MembershipPlan.CREATOR
            ? creatorMembershipPriceInr
            : investorMembershipPriceInr;

    PaymentType paymentType =
        plan == MembershipPlan.CREATOR
            ? PaymentType.MEMBERSHIP_CREATOR
            : PaymentType.MEMBERSHIP_INVESTOR;

    try {
      RazorpayClient client = razorpayConfig.razorpayClient();
      JSONObject options = new JSONObject();
      options.put("amount", (long)(price * 100));
      options.put("currency", "INR");
      options.put("receipt", "membership_" + plan.name().toLowerCase() + "_" + System.currentTimeMillis());

      Order order = client.orders.create(options);

      Payment payment =
          Payment.builder()
              .userId(user.getId())
              .amount(price)
              .status("PENDING")
              .paymentType(paymentType)
              .razorpayOrderId(order.get("id"))
              .createdAt(LocalDateTime.now())
              .build();

      paymentRepository.save(payment);

      return new CreateOrderResponse(order.get("id"), price);
    } catch (CustomException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new CustomException("Unable to start membership payment. Please try again.", 500);
    }
  }

  public VerifyPaymentResponse verifyMembershipPayment(User user, VerifyPaymentRequest request) {
    if (!verifyRazorpaySignature(request)) {
      return VerifyPaymentResponse.builder()
          .success(false)
          .message("Payment signature could not be verified. Contact support if money was debited.")
          .build();
    }

    Payment payment =
        paymentRepository
            .findByRazorpayOrderId(request.getRazorpayOrderId())
            .orElseThrow(() -> new CustomException("Membership payment record not found.", 404));

    if (!payment.getUserId().equals(user.getId())) {
      throw new CustomException("You are not allowed to verify this payment.", 403);
    }

    if (payment.getPaymentType() != PaymentType.MEMBERSHIP_CREATOR
        && payment.getPaymentType() != PaymentType.MEMBERSHIP_INVESTOR) {
      throw new CustomException("This order is not a membership payment.", 400);
    }

    if ("SUCCESS".equals(payment.getStatus())) {
      return VerifyPaymentResponse.builder()
          .success(true)
          .message("Membership is already active.")
          .receipt(buildMembershipReceipt(payment, user))
          .build();
    }

    payment.setStatus("SUCCESS");
    payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
    payment.setReceiptNumber(generateReceiptNumber(payment.getId()));
    paymentRepository.save(payment);

    User fresh = userRepository.findById(user.getId()).orElse(user);
    if (payment.getPaymentType() == PaymentType.MEMBERSHIP_CREATOR) {
      fresh.setCreatorMembershipActive(true);
    } else {
      fresh.setInvestorMembershipActive(true);
    }
    userRepository.save(fresh);

    return VerifyPaymentResponse.builder()
        .success(true)
        .message("Membership activated successfully.")
        .receipt(buildMembershipReceipt(payment, fresh))
        .build();
  }

  private SubscriptionStatusResponse buildCreatorStatus(User user) {
    int count = projectRepository.findByCreatorId(user.getId()).size();
    boolean hasMembership = user.isCreatorMembershipActive();
    boolean canCreate =
        hasMembership || (count < freeMaxProjects);

    String guidance;
    if (hasMembership) {
      guidance = "Creator Membership is active. You can list unlimited projects with any goal amount.";
    } else if (count >= freeMaxProjects) {
      guidance =
          "You have used your free project slot. Purchase Creator Membership to list another campaign.";
    } else {
      guidance =
          "Free plan: "
              + (freeMaxProjects - count)
              + " project slot left, max goal ₹"
              + (long)freeMaxGoalInr
              + ".";
    }

    return SubscriptionStatusResponse.builder()
        .role(Role.CREATOR.name())
        .hasMembership(hasMembership)
        .freeProjectLimit(freeMaxProjects)
        .projectsCreated(count)
        .freeGoalLimitInr(freeMaxGoalInr)
        .membershipPriceInr(creatorMembershipPriceInr)
        .canCreateProject(canCreate)
        .canInvest(true)
        .guidanceMessage(guidance)
        .build();
  }

  private SubscriptionStatusResponse buildInvestorStatus(
      User user, Long projectId, Double amount) {
    Set<Long> used = getInvestorProjectIds(user.getId());
    boolean hasMembership = user.isInvestorMembershipActive();
    boolean amountOk = amount == null || amount <= freeMaxInvestmentInr || hasMembership;
    boolean projectOk =
        hasMembership
        || projectId == null
        || used.contains(projectId)
        || used.size() < freeMaxInvestorProjects;

    String guidance;
    if (hasMembership) {
      guidance = "Investor Membership is active. You can invest in any number of projects without amount caps.";
    } else {
      guidance =
          "Free plan: invest up to ₹"
              + (long)freeMaxInvestmentInr
              + " on "
              + freeMaxInvestorProjects
              + " project. You have used "
              + used.size()
              + " project slot(s).";
    }

    return SubscriptionStatusResponse.builder()
        .role(Role.INVESTOR.name())
        .hasMembership(hasMembership)
        .freeInvestmentLimitInr(freeMaxInvestmentInr)
        .freeProjectInvestLimit(freeMaxInvestorProjects)
        .distinctProjectsUsed(used.size())
        .membershipPriceInr(investorMembershipPriceInr)
        .canCreateProject(false)
        .canInvest(amountOk && projectOk)
        .guidanceMessage(guidance)
        .build();
  }

  private Set<Long> getInvestorProjectIds(Long investorId) {
    Set<Long> ids = new HashSet<>();
    investmentRepository.findByInvestor_Id(investorId).forEach(
        inv -> ids.add(inv.getProject().getId()));
    List<InvestmentRequest> requests =
        investmentRequestRepository.findByInvestorId(investorId);
    for (InvestmentRequest req : requests) {
      if (req.getStatus() != InvestmentRequest.Status.REJECTED) {
        ids.add(req.getProjectId());
      }
    }
    return ids;
  }

  private PaymentReceiptResponse buildMembershipReceipt(Payment payment, User user) {
    return PaymentReceiptResponse.builder()
        .paymentId(payment.getId())
        .receiptNumber(payment.getReceiptNumber())
        .razorpayOrderId(payment.getRazorpayOrderId())
        .razorpayPaymentId(payment.getRazorpayPaymentId())
        .amount(payment.getAmount())
        .currency("INR")
        .status(payment.getStatus())
        .investorName(user.getName())
        .investorEmail(user.getEmail())
        .paidAt(payment.getCreatedAt())
        .build();
  }

  public String generateReceiptNumber(Long paymentId) {
    return "CB-" + paymentId + "-" + System.currentTimeMillis();
  }

  private boolean verifyRazorpaySignature(VerifyPaymentRequest request) {
    try {
      String data = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();
      javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
      javax.crypto.spec.SecretKeySpec secretKey =
          new javax.crypto.spec.SecretKeySpec(razorpaySecret.getBytes(), "HmacSHA256");
      mac.init(secretKey);
      byte[] hash = mac.doFinal(data.getBytes());
      String generated =
          new String(org.apache.commons.codec.binary.Hex.encodeHex(hash));
      return generated.equals(request.getRazorpaySignature());
    } catch (Exception ex) {
      return false;
    }
  }
}
