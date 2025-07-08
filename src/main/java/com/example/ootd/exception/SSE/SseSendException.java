package com.example.ootd.exception.SSE;

import com.example.ootd.exception.ErrorCode;
import com.example.ootd.exception.message.MessageException;

public class SseSendException extends SseException {

  public SseSendException(ErrorCode errorCode) {
    super(errorCode);
  }

  public SseSendException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }
}
