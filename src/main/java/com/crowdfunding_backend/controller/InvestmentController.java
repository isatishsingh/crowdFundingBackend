package com.crowdfunding_backend.controller;

import com.crowdfunding_backend.dto.investmentRequest.*;
import com.crowdfunding_backend.entity.User;
import com.crowdfunding_backend.service.InvestmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/investments")
public class InvestmentController {

  @Autowired private InvestmentService investmentService;

  // @PostMapping
  public InvestmentRequestResponse invest(Authentication authentication,
                                   @RequestBody CreateInvestmentRequest request) {
    User user = (User)authentication.getPrincipal(); // cast to User

    String email = user.getEmail(); // correct email

    // System.out.println("Logged user email: " + email);
    return investmentService.invest(email, request);
  }
}
