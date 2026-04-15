package com.crowdfunding_backend.controller;

import com.crowdfunding_backend.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/webhook")
public class WebhookController {

  @Autowired private PaymentService paymentService;

  @PostMapping("/razorpay")
  public String
  handleWebhook(HttpServletRequest request,
                @RequestHeader("X-Razorpay-Signature") String signature) {
    try {
      StringBuilder payload = new StringBuilder();
      BufferedReader reader = request.getReader();
      String line;

      while ((line = reader.readLine()) != null) {
        payload.append(line);
      }

      paymentService.processWebhook(payload.toString(), signature);

      return "Webhook processed";

    } catch (Exception e) {
      return "Webhook failed: " + e.getMessage();
    }
  }
}