package com.crowdfunding_backend.repository;

import com.crowdfunding_backend.entity.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
  Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
}