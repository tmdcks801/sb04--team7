package com.example.ootd.exception.notification;

import com.example.ootd.exception.ErrorCode;

public class NotificationCreateError extends NotificationException {

  public NotificationCreateError(ErrorCode errorCode) {
    super(errorCode);
  }

  public NotificationCreateError(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }
}
