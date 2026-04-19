package com.crowdfunding_backend.dto.project;

import java.time.LocalDateTime;

/**
 * Public-safe row for completed investments on a project (no passwords or emails).
 */
public record ProjectInvestorResponse(
    String investorName, Double amount, Double equityPercentage, LocalDateTime investedAt) {}
