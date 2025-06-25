package com.example.ootd.exception;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public class OotdException extends RuntimeException {

  // TODO: 요구사항에 timestamp 없음 의논 필요
  private final ErrorCode errorCode;
  private final Map<String, Object> details;

  public OotdException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
    this.details = new HashMap<>();
  }

  public OotdException(ErrorCode errorCode, Throwable cause) {
    super(errorCode.getMessage(), cause);
    this.errorCode = errorCode;
    this.details = new HashMap<>();
  }

  public OotdException(ErrorCode errorCode, Map<String, Object> details) {
    this(errorCode);
    this.details.putAll(details);
  }

  public OotdException(ErrorCode errorCode, Map<String, Object> details, Throwable cause) {
    this(errorCode, cause);
    this.details.putAll(details);
  }

  public void addDetail(String key, Object value) {
    this.details.put(key, value);

  public OotdException(String message) {
    super(message);
  }
}
