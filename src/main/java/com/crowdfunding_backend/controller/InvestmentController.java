package com.crowdfunding_backend.controller;

import com.crowdfunding_backend.dto.investment.*;
import com.crowdfunding_backend.service.InvestmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/investments")
public class InvestmentController {

  @Autowired private InvestmentService investmentService;

  @PostMapping
  public InvestmentResponse invest(@AuthenticationPrincipal String email,
                                   @RequestBody InvestmentRequest request) {
    return investmentService.invest(email, request);
  }
}