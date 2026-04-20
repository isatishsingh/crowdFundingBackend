package com.crowdfunding_backend.controller;

import com.crowdfunding_backend.dto.investmentRequest.*;
import com.crowdfunding_backend.entity.InvestmentRequest;
import com.crowdfunding_backend.entity.User;
import com.crowdfunding_backend.service.InvestmentRequestService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/investment-request")
public class InvestmentRequestController {

  @Autowired private InvestmentRequestService service;

  @PostMapping
  public InvestmentRequestResponse
  create(Authentication auth, @RequestBody CreateInvestmentRequest request) {

    User user = (User)auth.getPrincipal();
    return service.createRequest(user.getEmail(), request);
  }

  @GetMapping("/customer")
  public List<InvestmentRequest> getMyProjectRequests(Authentication auth) {

    User user = (User)auth.getPrincipal();

    return service.getRequestsForCustomer(user.getId());
  }

  /**
   * Lists all investment requests for the logged-in investor (needed for
   * dashboard + Pay after approval).
   */
  @GetMapping("/investor")
  @PreAuthorize("hasRole('INVESTOR')")
  public List<InvestmentRequest> getInvestorRequests(Authentication auth) {

    User user = (User)auth.getPrincipal();

    return service.getInvestorRequests(user.getId());
  }

  @PostMapping("/{id}/approve")
  public String approve(Authentication auth, @PathVariable Long id) {
    User user = (User)auth.getPrincipal();
    service.approve(id, user.getId());
    return "Approved";
  }

  @PostMapping("/{id}/reject")
  public String reject(Authentication auth, @PathVariable Long id) {

    User user = (User)auth.getPrincipal();

    service.reject(id, user.getId());

    return "Rejected";
  }
}