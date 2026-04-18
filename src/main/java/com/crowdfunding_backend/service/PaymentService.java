package com.crowdfunding_backend.service;

import com.crowdfunding_backend.config.RazorpayConfig;
import com.crowdfunding_backend.dto.payment.CreateOrderResponse;
// Your entity
import com.crowdfunding_backend.entity.*;
import com.crowdfunding_backend.exception.CustomException;
import com.crowdfunding_backend.repository.*;
import com.razorpay.Order;
// Razorpay
import com.razorpay.RazorpayClient;
// Java time
import java.time.LocalDateTime;
// JSON
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

  @Autowired private PaymentRepository paymentRepository;

  @Autowired private RazorpayConfig razorpayConfig;

  @Autowired private ProjectRepository projectRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private PaymentDetailsValidation validation;

  @Autowired private InvestmentRequestRepository investmentRequestRepository;

  @Value("${razorpay.secret}") private String razorpaySecret;
  @Value("${razorpay.webhook.secret}") private String razorpayWebhookSecret;

  public CreateOrderResponse createOrder(Long userId, Long requestId)
      throws Exception {

    // Fetch request
    InvestmentRequest request =
        investmentRequestRepository.findById(requestId).orElseThrow(
            () -> new CustomException("Request not found", 404));

    // Security check
    if (!request.getInvestorId().equals(userId)) {
      throw new CustomException("Unauthorized access", 403);
    }

    // Only approved requests allowed
    if (!request.getStatus().equals(InvestmentRequest.Status.APPROVED)) {
      throw new CustomException("Request not approved", 400);
    }

    User investor = userRepository.findById(userId).orElseThrow(
        () -> new CustomException("User not found", 404));

    Project project =
        projectRepository.findById(request.getProjectId())
            .orElseThrow(() -> new CustomException("Project not found", 404));

    // REUSE VALIDATION
    validation.validateInvestment(investor, project, request.getAmount(),
                                  request.getEquityPercentage());

    RazorpayClient client = razorpayConfig.razorpayClient();

    JSONObject options = new JSONObject();
    options.put("amount", request.getAmount() * 100);
    options.put("currency", "INR");
    options.put("receipt", "txn_" + System.currentTimeMillis());

    Order order = client.orders.create(options);

    Payment payment =
        Payment.builder()
            .userId(userId)
            .projectId(request.getProjectId())
            .amount(request.getAmount())
            .equityPercentage(request.getEquityPercentage())
            .status("PENDING")
            .razorpayOrderId(order.get("id"))
            .createdAt(LocalDateTime.now())
            .investmentRequestId(request.getId()) // VERY IMPORTANT
            .build();

    paymentRepository.save(payment);

    return new CreateOrderResponse(order.get("id"), request.getAmount());
  }

  public void processWebhook(String payload, String signature)
      throws Exception {

    // VERIFY WEBHOOK SIGNATURE
    String generatedSignature =
        validation.hmacSHA256(payload, razorpayWebhookSecret);

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
      validation.createInvestmentFromPayment(payment);
    }
  }
}