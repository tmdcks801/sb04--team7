package com.example.ootd.exception.feed;

import com.example.ootd.exception.ErrorCode;
import com.example.ootd.exception.OotdException;

public class FeedException extends OotdException {

  public FeedException(ErrorCode errorCode) {
    super(errorCode);
  }

  public FeedException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);

  }
}
