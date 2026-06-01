package com.crowdfunding_backend.exception;

public class CustomException extends RuntimeException {

  private final int statusCode;
  private final String code;

  public CustomException(String message, int statusCode) {
    this(message, statusCode, null);
  }

  public CustomException(String message, int statusCode, String code) {
    super(message);
    this.statusCode = statusCode;
    this.code = code;
  }

  public int getStatusCode() { return statusCode; }

  public String getCode() { return code; }
}