package com.crowdfunding_backend.controller;

import com.crowdfunding_backend.dto.payment.*;
import com.crowdfunding_backend.entity.User;
import com.crowdfunding_backend.service.PaymentDetailsValidation;
import com.crowdfunding_backend.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

  @Autowired private PaymentService paymentService;
  @Autowired private PaymentDetailsValidation validation;

  @PostMapping("/create-order")
  public CreateOrderResponse
  createOrder(Authentication authentication,
              @RequestBody CreateOrderRequest request) throws Exception {

    User user = (User)authentication.getPrincipal();

    // return paymentService.createOrder(user.getId(), request.getProjectId(),
    //                                   request.getAmount(),
    //                                   request.getEquityPercentage());
    return paymentService.createOrder(user.getId(),
                                      request.getInvestmentRequestId());
  }

  @PostMapping("/verify")
  public String verifyPayment(@RequestBody VerifyPaymentRequest request) {

    boolean isValid = validation.verifyPayment(request);

    if (isValid) {
      return "Payment verified successfully";
    } else {
      return "Payment verification failed";
    }
  }
}
