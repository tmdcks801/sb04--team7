package com.example.ootd.exception.user;

import com.example.ootd.exception.ErrorCode;
import java.util.UUID;

public class UserIdNotFoundException extends UserException {

  public UserIdNotFoundException() {
    super(ErrorCode.USER_ID_NOT_FOUND);
  }

  public static UserIdNotFoundException withId(UUID userId) {
    UserIdNotFoundException exception = new UserIdNotFoundException();
    exception.addDetail("userId", userId);
    return exception;
  }
}
