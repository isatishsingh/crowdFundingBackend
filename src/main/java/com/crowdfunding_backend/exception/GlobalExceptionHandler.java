package com.crowdfunding_backend.exception;

import java.time.LocalDateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;


@RestControllerAdvice
public class GlobalExceptionHandler {

  // ✅ Handle Custom Exceptions
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse>
  handleValidationException(MethodArgumentNotValidException ex) {

    String message = ex.getBindingResult().getFieldErrors().stream()
                           .findFirst()
                           .map(err -> err.getDefaultMessage())
                           .orElse("The submitted verification details are not valid.");

    ErrorResponse error = ErrorResponse.builder()
                              .message(message)
                              .code("VALIDATION_ERROR")
                              .status(400)
                              .timestamp(LocalDateTime.now())
                              .build();

    return ResponseEntity.status(400).body(error);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse>
  handleAccessDeniedException(AccessDeniedException ex) {

    ErrorResponse error = ErrorResponse.builder()
                              .message(
                                  "You do not have permission to perform this action.")
                              .code("ACCESS_DENIED")
                              .status(403)
                              .timestamp(LocalDateTime.now())
                              .build();

    return ResponseEntity.status(403).body(error);
  }

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ErrorResponse>
  handleCustomException(CustomException ex) {

    ErrorResponse error = ErrorResponse.builder()
                              .message(ex.getMessage())
                              .code(ex.getCode())
                              .status(ex.getStatusCode())
                              .timestamp(LocalDateTime.now())
                              .build();

    return ResponseEntity.status(ex.getStatusCode()).body(error);
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {

    if (ex instanceof CustomException custom) {
      return handleCustomException(custom);
    }

    String message =
        ex.getMessage() != null && !ex.getMessage().isBlank()
            ? ex.getMessage()
            : "The request could not be completed. Please try again.";

    ErrorResponse error = ErrorResponse.builder()
                              .message(message)
                              .status(400)
                              .timestamp(LocalDateTime.now())
                              .build();

    return ResponseEntity.status(400).body(error);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {

    ErrorResponse error = ErrorResponse.builder()
                              .message(
                                  "An unexpected server error occurred. Please try again in a few minutes.")
                              .status(500)
                              .timestamp(LocalDateTime.now())
                              .build();

    return ResponseEntity.status(500).body(error);
  }
}