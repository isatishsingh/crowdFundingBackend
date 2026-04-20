package com.crowdfunding_backend.service;

import com.crowdfunding_backend.dto.investmentRequest.CreateInvestmentRequest;
import com.crowdfunding_backend.dto.investmentRequest.InvestmentRequestResponse;
import com.crowdfunding_backend.entity.*;
import com.crowdfunding_backend.exception.CustomException;
import com.crowdfunding_backend.repository.*;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InvestmentRequestService {

  @Autowired private InvestmentRequestRepository requestRepository;
  @Autowired private ProjectRepository projectRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private PaymentDetailsValidation validation;

  // CREATE REQUEST
  public InvestmentRequestResponse
  createRequest(String email, CreateInvestmentRequest request) {

    User investor = userRepository.findByEmailIgnoreCase(email).orElseThrow(
        () -> new CustomException("User not found", 404));

    Project project =
        projectRepository.findById(request.getProjectId())
            .orElseThrow(() -> new CustomException("Project not found", 404));

    validation.validateInvestment(investor, project, request.getAmount(),
                                  request.getEquityPercentage());
    double normalizedEquity =
        validation.normalizeEquity(request.getEquityPercentage());

    InvestmentRequest entity =
        InvestmentRequest.builder()
            .investorId(investor.getId())
            .projectId(project.getId())
            .amount(request.getAmount())
            .equityPercentage(normalizedEquity)
            .status(InvestmentRequest.Status.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

    requestRepository.save(entity);

    return InvestmentRequestResponse.builder()
        .id(entity.getId())
        .projectId(entity.getProjectId())
        .amount(entity.getAmount())
        .equityPercentage(entity.getEquityPercentage())
        .status(entity.getStatus().name())
        .build();
  }

  // APPROVE
  public void approve(Long requestId, Long customerId) {

    InvestmentRequest req = requestRepository.findById(requestId).orElseThrow(
        () -> new CustomException("Request not found", 404));

    Project project =
        projectRepository.findById(req.getProjectId())
            .orElseThrow(() -> new CustomException("Project not found", 404));

    // SECURITY CHECK
    if (!project.getCreator().getId().equals(customerId)) {
      throw new CustomException("Unauthorized", 403);
    }

    req.setStatus(InvestmentRequest.Status.APPROVED);
    requestRepository.save(req);
  }

  // REJECT
  public void reject(Long requestId, Long customerId) {

    InvestmentRequest req = requestRepository.findById(requestId).orElseThrow(
        () -> new CustomException("Request not found", 404));

    Project project =
        projectRepository.findById(req.getProjectId())
            .orElseThrow(() -> new CustomException("Project not found", 404));

    if (!project.getCreator().getId().equals(customerId)) {
      throw new CustomException("Unauthorized", 403);
    }

    req.setStatus(InvestmentRequest.Status.REJECTED);
    requestRepository.save(req);
  }

  public InvestmentRequest getById(Long id) {
    return requestRepository.findById(id).orElseThrow(
        () -> new CustomException("Request not found", 404));
  }

  public List<InvestmentRequest> getRequestsForCustomer(Long customerId) {

    List<Project> projects = projectRepository.findByCreatorId(customerId);

    List<Long> projectIds = projects.stream().map(Project::getId).toList();

    List<InvestmentRequest> list =
        requestRepository.findByProjectIdIn(projectIds);

    // 🔥 SORT HERE
    list.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

    return list;
  }

  public List<InvestmentRequest> getInvestorRequests(Long investorId) {

    List<InvestmentRequest> list =
        requestRepository.findByInvestorId(investorId);

    // 🔥 SORT HERE
    list.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

    return list;
  }
}