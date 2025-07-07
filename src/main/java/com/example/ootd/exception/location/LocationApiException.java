package com.example.ootd.exception.location;

import com.example.ootd.exception.ErrorCode;

//Location API 호출 실패 시
public class LocationApiException extends LocationException {

  public LocationApiException(Throwable cause) {
    super(ErrorCode.LOCATION_API_ERROR, cause);
  }

  public LocationApiException(double latitude, double longitude, Throwable cause) {
    super(ErrorCode.LOCATION_API_ERROR, cause);
    addDetail("latitude", latitude);
    addDetail("longitude", longitude);
  }
}
