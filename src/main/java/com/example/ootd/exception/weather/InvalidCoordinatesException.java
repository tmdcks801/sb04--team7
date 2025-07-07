package com.example.ootd.exception.weather;

import com.example.ootd.exception.ErrorCode;

//잘못된 좌표값
public class InvalidCoordinatesException extends WeatherException {

  public InvalidCoordinatesException(double latitude, double longitude) {
    super(ErrorCode.INVALID_COORDINATES);
    addDetail("latitude", latitude);
    addDetail("longitude", longitude);
  }
}
