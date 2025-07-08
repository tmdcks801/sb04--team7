package com.example.ootd.exception.SSE;

import com.example.ootd.exception.ErrorCode;
import com.example.ootd.exception.OotdException;

public class SseException extends OotdException {

  public SseException(ErrorCode errorCode) {
    super(errorCode);
  }

  public SseException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }
}
