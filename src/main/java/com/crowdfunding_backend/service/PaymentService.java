package com.crowdfunding_backend.service;

import com.crowdfunding_backend.config.RazorpayConfig;
import com.crowdfunding_backend.dto.payment.CreateOrderResponse;
import com.crowdfunding_backend.dto.payment.VerifyPaymentRequest;
// Your entity
import com.crowdfunding_backend.entity.*;
import com.crowdfunding_backend.exception.CustomException;
import com.crowdfunding_backend.repository.*;
import com.razorpay.Order;
// Razorpay
import com.razorpay.RazorpayClient;
// import com.razorpay.RazorpayException;
// Java time
import java.time.LocalDateTime;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
// JSON
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

  @Autowired private PaymentRepository paymentRepository;

  @Autowired private RazorpayConfig razorpayConfig;

  @Autowired private InvestmentRepository investmentRepository;

  @Autowired private ProjectRepository projectRepository;

  @Autowired private UserRepository userRepository;

  @Value("${razorpay.secret}") private String razorpaySecret;
  @Value("${razorpay.webhook.secret}") private String razorpayWebhookSecret;

  public CreateOrderResponse createOrder(Long userId, Long projectId,
                                         Double amount, Double equityPercentage)
      throws Exception {
    User investor = userRepository.findById(userId).orElseThrow(
        () -> new CustomException("User not found", 404));

    Project project = projectRepository.findById(projectId).orElseThrow(
        () -> new CustomException("Project not found", 404));

    // REUSE VALIDATION
    validateInvestment(investor, project, amount, equityPercentage);

    RazorpayClient client = razorpayConfig.razorpayClient();

    JSONObject options = new JSONObject();
    options.put("amount", amount * 100); // paisa
    options.put("currency", "INR");
    options.put("receipt", "txn_" + System.currentTimeMillis());

    Order order = client.orders.create(options);

    Payment payment = Payment.builder()
                          .userId(userId)
                          .projectId(projectId)
                          .amount(amount)
                          .status("PENDING")
                          .razorpayOrderId(order.get("id"))
                          .createdAt(LocalDateTime.now())
                          .equityPercentage(equityPercentage)
                          .build();

    paymentRepository.save(payment);

    return new CreateOrderResponse(order.get("id"), amount);
  }

  public boolean verifyPayment(VerifyPaymentRequest request) {

    try {
      String data =
          request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();

      String generatedSignature = hmacSHA256(data, razorpaySecret);

      if (!generatedSignature.equals(request.getRazorpaySignature())) {
        throw new CustomException("Invalid payment signature", 400);
      }

      // FETCH PAYMENT FROM DB
      Payment payment =
          paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
              .orElseThrow(() -> new CustomException("Payment not found", 404));

      // CHECK STATUS
      if (!payment.getStatus().equals("PENDING")) {
        throw new CustomException("Payment already processed", 400);
      }

      // OPTIONAL: FETCH FROM RAZORPAY FOR DOUBLE CHECK
      // Payment razorpayPayment =
      // razorpayClient.payments.fetch(request.getRazorpayPaymentId());

      // MARK SUCCESS
      payment.setStatus("SUCCESS");
      payment.setRazorpayPaymentId(request.getRazorpayPaymentId());

      paymentRepository.save(payment);

      // CREATE INVESTMENT HERE (IMPORTANT)
      createInvestmentFromPayment(payment);

      return true;

    } catch (Exception e) {

      throw new CustomException(
          "Payment verification failed: " + e.getMessage(), 400);
    }
  }

  private String hmacSHA256(String data, String secret) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    SecretKeySpec secretKey =
        new SecretKeySpec(secret.getBytes(), "HmacSHA256");
    mac.init(secretKey);

    byte[] hash = mac.doFinal(data.getBytes());

    return new String(org.apache.commons.codec.binary.Hex.encodeHex(hash));
  }

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

    double remainingAmount =
        project.getGoalAmount() - project.getCurrentAmount();

    double remainingEquity =
        project.getTotalEquityOffered() - project.getEquityAllocated();

    // OverFunding check
    if (amount > remainingAmount) {
      throw new CustomException("Investment exceeds remaining amount", 400);
    }

    // Over equity check
    if (equityPercentage > remainingEquity) {
      throw new CustomException("Equity exceeds remaining equity", 400);
    }
  }

  public void createInvestmentFromPayment(Payment payment) {

    // Fetch user
    User investor =
        userRepository.findById(payment.getUserId())
            .orElseThrow(() -> new CustomException("User not found", 404));

    // Fetch project
    Project project =
        projectRepository.findById(payment.getProjectId())
            .orElseThrow(() -> new CustomException("Project not found", 404));

    // REUSE VALIDATION
    validateInvestment(investor, project, payment.getAmount(),
                       payment.getEquityPercentage());

    // Create investment (CORRECT WAY)
    Investment investment = Investment.builder()
                                .investor(investor)
                                .project(project)
                                .amount(payment.getAmount())
                                .equityPercentage(payment.getEquityPercentage())
                                .build();

    // Update project funding
    project.setCurrentAmount(project.getCurrentAmount() + payment.getAmount());

    // update equity allocated
    project.setEquityAllocated(project.getEquityAllocated() +
                               payment.getEquityPercentage());

    projectRepository.save(project);
    investmentRepository.save(investment);
  }

  public void processWebhook(String payload, String signature)
      throws Exception {

    // VERIFY WEBHOOK SIGNATURE
    String generatedSignature = hmacSHA256(payload, razorpayWebhookSecret);

    if (!generatedSignature.equals(signature)) {
      throw new CustomException("Invalid webhook signature", 400);
    }

    JSONObject json = new JSONObject(payload);

    String event = json.getString("event");

    // HANDLE PAYMENT SUCCESS
    if ("payment.captured".equals(event)) {

      JSONObject paymentEntity =
          json.getJSONObject("payload").getJSONObject("payment").getJSONObject(
              "entity");

      String orderId = paymentEntity.getString("order_id");
      String paymentId = paymentEntity.getString("id");

      Payment payment =
          paymentRepository.findByRazorpayOrderId(orderId).orElseThrow(
              () -> new CustomException("Payment not found", 404));

      // PREVENT DUPLICATE
      if (!payment.getStatus().equals("PENDING")) {
        return;
      }

      // MARK SUCCESS
      payment.setStatus("SUCCESS");
      payment.setRazorpayPaymentId(paymentId);

      paymentRepository.save(payment);

      // CREATE INVESTMENT
      createInvestmentFromPayment(payment);
    }
  }
}