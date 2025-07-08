package com.example.ootd.exception.message;

import com.example.ootd.exception.ErrorCode;

public class FailGetMessageExecption extends MessageException {

  public FailGetMessageExecption(ErrorCode errorCode) {
    super(errorCode);
  }

  public FailGetMessageExecption(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }
}
