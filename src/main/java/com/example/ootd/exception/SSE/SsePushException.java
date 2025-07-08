package com.example.ootd.exception.SSE;

import com.example.ootd.exception.ErrorCode;

public class SsePushException extends SseException {

  public SsePushException(ErrorCode errorCode) {
    super(errorCode);
  }

  public SsePushException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }
}

