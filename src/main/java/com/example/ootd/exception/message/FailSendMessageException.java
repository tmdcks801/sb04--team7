package com.example.ootd.exception.message;

import com.example.ootd.exception.ErrorCode;

public class FailSendMessageException extends MessageException {

  public FailSendMessageException(ErrorCode errorCode) {
    super(errorCode);
  }

  public FailSendMessageException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }
}
