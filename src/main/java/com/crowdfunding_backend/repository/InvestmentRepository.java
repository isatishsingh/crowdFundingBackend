package com.crowdfunding_backend.repository;

import com.crowdfunding_backend.entity.Investment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


public interface InvestmentRepository extends JpaRepository<Investment, Long> {

  List<Investment> findByInvestor_Id(Long investorId);

  List<Investment> findByProject_Id(Long projectId);
}