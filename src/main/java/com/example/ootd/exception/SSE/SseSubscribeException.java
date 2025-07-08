package com.example.ootd.exception.SSE;

import com.example.ootd.exception.ErrorCode;
import com.example.ootd.exception.message.MessageException;

public class SseSubscribeException extends SseException {

  public SseSubscribeException(ErrorCode errorCode) {
    super(errorCode);
  }

  public SseSubscribeException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }
}
