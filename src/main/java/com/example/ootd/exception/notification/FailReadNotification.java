package com.example.ootd.exception.notification;

import com.example.ootd.exception.ErrorCode;

public class FailReadNotification extends NotificationException {

  public FailReadNotification(ErrorCode errorCode) {
    super(errorCode);
  }

  public FailReadNotification(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }
}
