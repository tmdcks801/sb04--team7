package com.example.ootd.exception.user;

import com.example.ootd.exception.ErrorCode;
import com.example.ootd.exception.OotdException;

public class UserException extends OotdException {

  public UserException(ErrorCode errorCode) {
    super(errorCode);
  }

  public UserException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }
}
