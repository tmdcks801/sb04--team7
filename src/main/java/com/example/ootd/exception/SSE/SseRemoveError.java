package com.example.ootd.exception.SSE;

import com.example.ootd.exception.ErrorCode;
import com.example.ootd.exception.message.MessageException;

public class SseRemoveError extends SseException {

  public SseRemoveError(ErrorCode errorCode) {
    super(errorCode);
  }

  public SseRemoveError(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }
}
