package com.crowdfunding_backend.service;

import com.crowdfunding_backend.dto.payment.PaymentReceiptResponse;
import com.crowdfunding_backend.dto.payment.VerifyPaymentRequest;
import com.crowdfunding_backend.dto.payment.VerifyPaymentResponse;
import com.crowdfunding_backend.entity.*;
import com.crowdfunding_backend.exception.CustomException;
import com.crowdfunding_backend.repository.InvestmentRepository;
import com.crowdfunding_backend.repository.InvestmentRequestRepository;
import com.crowdfunding_backend.repository.PaymentRepository;
import com.crowdfunding_backend.repository.ProjectRepository;
import com.crowdfunding_backend.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PaymentDetailsValidation {

  @Autowired private PaymentRepository paymentRepository;
  @Autowired private ProjectRepository projectRepository;
  @Autowired private InvestmentRepository investmentRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private InvestmentRequestRepository investmentRequestRepository;

  @Autowired private SubscriptionService subscriptionService;

  @Value("${razorpay.secret}") private String razorpaySecret;

  public void validateInvestment(User investor, Project project, Double amount,
                                 Double equityPercentage) {

    // Role validation
    if (!investor.getRole().name().equals("INVESTOR")) {
      throw new CustomException("Only investors can invest", 403);
    }

    // Self investment restriction
    if (project.getCreator().getId().equals(investor.getId())) {
      throw new CustomException("Cannot invest in your own project", 400);
    }

    // Deadline validation
    if (project.getDeadline().isBefore(LocalDateTime.now())) {
      throw new CustomException("Project deadline passed", 400);
    }

    // Amount validation
    if (amount == null || amount <= 0) {
      throw new CustomException("Invalid amount", 400);
    }

    if (equityPercentage == null || equityPercentage <= 0) {
      throw new CustomException("Invalid equity", 400);
    }

    double normalizedEquity = normalizeEquity(equityPercentage);
    if (normalizedEquity < 0.1) {
      throw new CustomException("Minimum equity is 0.1%", 400);
    }

    double remainingAmount =
        project.getGoalAmount() - project.getCurrentAmount();

    double remainingEquity = normalizeEquity(
        project.getTotalEquityOffered() - project.getEquityAllocated());

    // OverFunding check
    if (amount > remainingAmount) {
      throw new CustomException("Investment exceeds remaining amount", 400);
    }

    // Over equity check
    BigDecimal requested = BigDecimal.valueOf(normalizedEquity);
    BigDecimal remaining = BigDecimal.valueOf(remainingEquity);
    if (requested.compareTo(remaining) > 0) {
      throw new CustomException("Equity exceeds remaining equity", 400);
    }

    subscriptionService.validateInvestorCanInvest(
        investor, project.getId(), amount);
  }

  public double normalizeEquity(Double equityPercentage) {
    if (equityPercentage == null) {
      return 0.0;
    }
    return BigDecimal.valueOf(equityPercentage)
        .setScale(1, RoundingMode.HALF_UP)
        .doubleValue();
  }

  public boolean verifySignatureOnly(VerifyPaymentRequest request) {
    try {
      String data =
          request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();
      String generatedSignature = hmacSHA256(data, razorpaySecret);
      return generatedSignature.equals(request.getRazorpaySignature());
    } catch (Exception ex) {
      return false;
    }
  }

  public VerifyPaymentResponse verifyInvestmentPayment(VerifyPaymentRequest request) {
    if (!verifySignatureOnly(request)) {
      return VerifyPaymentResponse.builder()
          .success(false)
          .message("Payment signature could not be verified. Contact support if money was debited.")
          .build();
    }

    Payment payment =
        paymentRepository
            .findByRazorpayOrderId(request.getRazorpayOrderId())
            .orElseThrow(
                () -> new CustomException("Payment record not found for this order.", 404));

    if (payment.getPaymentType() != PaymentType.INVESTMENT) {
      throw new CustomException("This order is not an investment payment.", 400);
    }

    if ("SUCCESS".equals(payment.getStatus())) {
      return VerifyPaymentResponse.builder()
          .success(true)
          .message("Payment was already recorded.")
          .receipt(buildReceipt(payment))
          .build();
    }

    payment.setStatus("SUCCESS");
    payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
    if (payment.getReceiptNumber() == null) {
      payment.setReceiptNumber(subscriptionService.generateReceiptNumber(payment.getId()));
    }
    paymentRepository.save(payment);

    createInvestmentFromPayment(payment);

    return VerifyPaymentResponse.builder()
        .success(true)
        .message("Payment verified successfully.")
        .receipt(buildReceipt(payment))
        .build();
  }

  public PaymentReceiptResponse getReceiptForUser(Long userId, Long paymentId) {
    Payment payment =
        paymentRepository
            .findById(paymentId)
            .orElseThrow(() -> new CustomException("Receipt not found.", 404));

    if (!payment.getUserId().equals(userId)) {
      throw new CustomException("You are not allowed to view this receipt.", 403);
    }

    if (!"SUCCESS".equals(payment.getStatus())) {
      throw new CustomException("Receipt is available only for successful payments.", 400);
    }

    return buildReceipt(payment);
  }

  public PaymentReceiptResponse buildReceipt(Payment payment) {
    Payment fresh =
        paymentRepository.findById(payment.getId()).orElse(payment);

    User investor =
        userRepository
            .findById(fresh.getUserId())
            .orElseThrow(() -> new CustomException("Investor not found", 404));

    Project project =
        projectRepository
            .findById(fresh.getProjectId())
            .orElseThrow(() -> new CustomException("Project not found", 404));

    Investment investment =
        investmentRepository.findByInvestor_Id(investor.getId()).stream()
            .filter(inv -> fresh.getId().equals(inv.getPaymentId()))
            .findFirst()
            .orElse(null);

    return PaymentReceiptResponse.builder()
        .paymentId(fresh.getId())
        .receiptNumber(fresh.getReceiptNumber())
        .razorpayOrderId(fresh.getRazorpayOrderId())
        .razorpayPaymentId(fresh.getRazorpayPaymentId())
        .amount(fresh.getAmount())
        .currency("INR")
        .status(fresh.getStatus())
        .projectId(project.getId())
        .projectTitle(project.getTitle())
        .equityPercentage(fresh.getEquityPercentage())
        .investorName(investor.getName())
        .investorEmail(investor.getEmail())
        .paidAt(fresh.getCreatedAt())
        .investmentId(investment != null ? investment.getId() : null)
        .build();
  }

  public String hmacSHA256(String data, String secret) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    SecretKeySpec secretKey =
        new SecretKeySpec(secret.getBytes(), "HmacSHA256");
    mac.init(secretKey);

    byte[] hash = mac.doFinal(data.getBytes());

    return new String(org.apache.commons.codec.binary.Hex.encodeHex(hash));
  }

  public void createInvestmentFromPayment(Payment payment) {

    // Fetch request FIRST (source of truth)
    InvestmentRequest request =
        investmentRequestRepository.findById(payment.getInvestmentRequestId())
            .orElseThrow(() -> new CustomException("Request not found", 404));

    // Prevent duplicate processing
    if (request.getStatus().equals(InvestmentRequest.Status.COMPLETED)) {
      return;
    }

    // Fetch user
    User investor =
        userRepository.findById(request.getInvestorId())
            .orElseThrow(() -> new CustomException("User not found", 404));

    // Fetch project
    Project project =
        projectRepository.findById(request.getProjectId())
            .orElseThrow(() -> new CustomException("Project not found", 404));

    // Create investment using REQUEST (NOT payment)
    Investment investment =
        Investment.builder()
            .investor(investor)
            .project(project)
            .amount(request.getAmount())
            .equityPercentage(request.getEquityPercentage())
            .paymentId(payment.getId())
            .build();

    if (payment.getReceiptNumber() == null) {
      payment.setReceiptNumber(subscriptionService.generateReceiptNumber(payment.getId()));
      paymentRepository.save(payment);
    }

    // Update project funding
    project.setCurrentAmount(project.getCurrentAmount() + request.getAmount());

    // Update equity
    double updatedEquity = normalizeEquity(
        project.getEquityAllocated() + request.getEquityPercentage());
    project.setEquityAllocated(updatedEquity);

    // Mark request completed
    request.setStatus(InvestmentRequest.Status.COMPLETED);

    // Save all
    projectRepository.save(project);
    investmentRepository.save(investment);
    investmentRequestRepository.save(request);
  }
}
