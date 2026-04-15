package com.crowdfunding_backend.config;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RazorpayConfig {

  @Value("${razorpay.key}") private String key;

  @Value("${razorpay.secret}") private String secret;

  public RazorpayClient razorpayClient() throws RazorpayException {
    return new RazorpayClient(key, secret);
  }
}