package com.example.ootd.exception.weather;

import com.example.ootd.exception.ErrorCode;
import com.example.ootd.exception.OotdException;

public class WeatherException extends OotdException {

  public WeatherException(ErrorCode errorCode) {
    super(errorCode);
  }

  public WeatherException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }
}
