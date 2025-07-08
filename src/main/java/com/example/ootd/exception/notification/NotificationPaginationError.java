package com.example.ootd.exception.notification;

import com.example.ootd.exception.ErrorCode;
import com.example.ootd.exception.OotdException;

public class NotificationPaginationError extends OotdException {

  public NotificationPaginationError(ErrorCode errorCode) {
    super(errorCode);
  }

  public NotificationPaginationError(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }
}

