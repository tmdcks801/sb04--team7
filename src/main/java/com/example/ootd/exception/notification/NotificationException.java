package com.example.ootd.exception.notification;

import com.example.ootd.exception.ErrorCode;
import com.example.ootd.exception.OotdException;

public class NotificationException extends OotdException {

  public NotificationException(ErrorCode errorCode) {
    super(errorCode);
  }

  public NotificationException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }
}
