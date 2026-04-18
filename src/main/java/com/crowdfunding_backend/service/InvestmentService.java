package com.crowdfunding_backend.service;

import com.crowdfunding_backend.dto.investmentRequest.*;
import com.crowdfunding_backend.entity.*;
import com.crowdfunding_backend.exception.CustomException;
import com.crowdfunding_backend.repository.*;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InvestmentService {

  @Autowired private InvestmentRepository investmentRepository;

  @Autowired private ProjectRepository projectRepository;

  @Autowired private UserRepository userRepository;

  public void createInvestmentFromPayment(Payment payment) {

    // Fetch user
    User investor =
        userRepository.findById(payment.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));

    // Fetch project
    Project project =
        projectRepository.findById(payment.getProjectId())
            .orElseThrow(() -> new RuntimeException("Project not found"));

    // Create investment (CORRECT WAY)
    Investment investment = Investment.builder()
                                .investor(investor)
                                .project(project)
                                .amount(payment.getAmount())
                                .equityPercentage(payment.getEquityPercentage())
                                .build();

    // Update project funding
    project.setCurrentAmount(project.getCurrentAmount() + payment.getAmount());

    projectRepository.save(project);
    investmentRepository.save(investment);
  }

  public InvestmentRequestResponse invest(String email, CreateInvestmentRequest request) {

    User investor = userRepository.findByEmailIgnoreCase(email).orElseThrow(
        () -> new CustomException("User not found", 404));

    // Role validation
    if (!investor.getRole().name().equals("INVESTOR")) {
      throw new CustomException("Only investors can invest", 403);
    }

    Project project =
        projectRepository.findById(request.getProjectId())
            .orElseThrow(() -> new CustomException("Project not found", 404));

    // Self investment restriction
    if (project.getCreator().getId().equals(investor.getId())) {
      throw new CustomException("Cannot invest in your own project", 400);
    }

    // Deadline validation
    if (project.getDeadline().isBefore(LocalDateTime.now())) {
      throw new CustomException("Project deadline passed", 400);
    }

    // Amount validation
    if (request.getAmount() <= 0) {
      throw new CustomException("Invalid amount", 400);
    }

    if (request.getEquityPercentage() <= 0) {
      throw new CustomException("Invalid equity", 400);
    }

    double remainingAmount =
        project.getGoalAmount() - project.getCurrentAmount();
    double remainingEquity =
        project.getTotalEquityOffered() - project.getEquityAllocated();

    // OverFunding check
    if (request.getAmount() > remainingAmount) {
      throw new CustomException("Investment exceeds remaining amount", 400);
    }

    // Over equity check
    if (request.getEquityPercentage() > remainingEquity) {
      throw new CustomException("Equity exceeds remaining equity", 400);
    }

    // Create investment
    Investment investment = Investment.builder()
                                .investor(investor)
                                .project(project)
                                .amount(request.getAmount())
                                .equityPercentage(request.getEquityPercentage())
                                .build();

    // Update project amount
    project.setCurrentAmount(project.getCurrentAmount() + request.getAmount());

    // Update equity allocation
    project.setEquityAllocated(project.getEquityAllocated() +
                               request.getEquityPercentage());

    projectRepository.save(project);
    investmentRepository.save(investment);

    return InvestmentRequestResponse.builder()
        .id(investment.getId())
        .investorId(investor.getId())
        .projectId(project.getId())
        .amount(investment.getAmount())
        .equityPercentage(investment.getEquityPercentage())
        .investedAt(investment.getInvestedAt())
        .build();
  }
}