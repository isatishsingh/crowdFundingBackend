package com.crowdfunding_backend.repository;

import com.crowdfunding_backend.entity.InvestmentRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestmentRequestRepository
    extends JpaRepository<InvestmentRequest, Long> {

  List<InvestmentRequest> findByProjectId(Long projectId);

  //  NEW: For CUSTOMER dashboard
  List<InvestmentRequest> findByProjectIdIn(List<Long> projectIds);

  //  NEW: For INVESTOR dashboard
  List<InvestmentRequest> findByInvestorId(Long investorId);

  //  NEW: Filter by status
  List<InvestmentRequest>
  findByProjectIdInAndStatus(List<Long> projectIds,
                             InvestmentRequest.Status status);

  List<InvestmentRequest>
  findByInvestorIdAndStatus(Long investorId, InvestmentRequest.Status status);
}