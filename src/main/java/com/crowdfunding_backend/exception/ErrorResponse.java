package com.crowdfunding_backend.exception;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class ErrorResponse {

  private String message;
  private String code;
  private int status;
  private LocalDateTime timestamp;
}