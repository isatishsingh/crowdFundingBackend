package com.crowdfunding_backend.service;

import com.crowdfunding_backend.dto.investment.*;
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

  public InvestmentResponse invest(String email, InvestmentRequest request) {

    User investor = userRepository.findByEmail(email).orElseThrow(
        () -> new CustomException("User not found", 404));

    // ✅ Role validation
    if (!investor.getRole().name().equals("ROLE_INVESTOR")) {
      throw new CustomException("Only investors can invest", 403);
    }

    Project project =
        projectRepository.findById(request.getProjectId())
            .orElseThrow(() -> new CustomException("Project not found", 404));

    // ❌ Self investment restriction
    if (project.getCreator().getId().equals(email)) {
      throw new CustomException("Cannot invest in your own project", 400);
    }

    // ❌ Deadline validation
    if (project.getDeadline().isBefore(LocalDateTime.now())) {
      throw new CustomException("Project deadline passed", 400);
    }

    // ❌ Amount validation
    if (request.getAmount() <= 0) {
      throw new CustomException("Invalid amount", 400);
    }

    // ❌ Overfunding check
    double updatedAmount = project.getCurrentAmount() + request.getAmount();

    if (updatedAmount > project.getGoalAmount()) {
      throw new CustomException("Investment exceeds goal amount", 400);
    }

    // ✅ Create investment
    Investment investment = Investment.builder()
                                .investor(investor)
                                .project(project)
                                .amount(request.getAmount())
                                .investedAt(LocalDateTime.now())
                                .build();

    // ✅ Update project amount
    project.setCurrentAmount(updatedAmount);

    projectRepository.save(project);
    investmentRepository.save(investment);

    return InvestmentResponse.builder()
        .id(investment.getId())
        .investorId(investor.getId())
        .projectId(project.getId())
        .amount(investment.getAmount())
        .investedAt(investment.getInvestedAt())
        .build();
  }
}