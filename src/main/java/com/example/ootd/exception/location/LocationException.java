package com.example.ootd.exception.location;

import com.example.ootd.exception.ErrorCode;
import com.example.ootd.exception.OotdException;

// Location 도메인의 기본 예외 클래스
public class LocationException extends OotdException {

  public LocationException(ErrorCode errorCode) {
    super(errorCode);
  }

  public LocationException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }
}
