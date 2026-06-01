package com.crowdfunding_backend.controller;

import com.crowdfunding_backend.dto.investment.InvestmentHistoryResponse;
import com.crowdfunding_backend.dto.investmentRequest.*;
import com.crowdfunding_backend.entity.User;
import com.crowdfunding_backend.service.InvestmentService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/investments")
public class InvestmentController {

  @Autowired private InvestmentService investmentService;

  @GetMapping("/history")
  @PreAuthorize("hasRole('INVESTOR')")
  public List<InvestmentHistoryResponse> getHistory(Authentication authentication) {
    User user = (User)authentication.getPrincipal();
    return investmentService.getInvestmentHistory(user.getId());
  }

  // @PostMapping
  public InvestmentRequestResponse invest(Authentication authentication,
                                   @RequestBody CreateInvestmentRequest request) {
    User user = (User)authentication.getPrincipal(); // cast to User

    String email = user.getEmail(); // correct email

    // System.out.println("Logged user email: " + email);
    return investmentService.invest(email, request);
  }
}
