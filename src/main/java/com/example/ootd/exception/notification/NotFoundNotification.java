package com.example.ootd.exception.notification;

import com.example.ootd.exception.ErrorCode;

public class NotFoundNotification extends NotificationException {

  public NotFoundNotification(ErrorCode errorCode) {
    super(errorCode);
  }

  public NotFoundNotification(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }
}
