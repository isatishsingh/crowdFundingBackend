package com.crowdfunding_backend.exception;

import java.time.LocalDateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestControllerAdvice
public class GlobalExceptionHandler {

  // ✅ Handle Custom Exceptions
  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ErrorResponse>
  handleCustomException(CustomException ex) {

    ErrorResponse error = ErrorResponse.builder()
                              .message(ex.getMessage())
                              .status(ex.getStatusCode())
                              .timestamp(LocalDateTime.now())
                              .build();

    return ResponseEntity.status(ex.getStatusCode()).body(error);
  }

  // ✅ Handle General Exceptions
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {

    ErrorResponse error = ErrorResponse.builder()
                              .message("Something went wrong")
                              .status(500)
                              .timestamp(LocalDateTime.now())
                              .build();

    return ResponseEntity.status(500).body(error);
  }
}