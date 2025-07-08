package com.example.ootd.exception.message;

import com.example.ootd.exception.ErrorCode;
import com.example.ootd.exception.OotdException;

public class MessageException extends OotdException {

  public MessageException(ErrorCode errorCode) {
    super(errorCode);
  }

  public MessageException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }
}
