package com.crowdfunding_backend.repository;

import com.crowdfunding_backend.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PaymentTransactionRepository
    extends JpaRepository<PaymentTransaction, Long> {}
