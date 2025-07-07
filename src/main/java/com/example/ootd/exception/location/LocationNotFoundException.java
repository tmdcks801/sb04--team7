package com.example.ootd.exception.location;

import com.example.ootd.exception.ErrorCode;

// 위치 정보를 찾을 수 없을 때
public class LocationNotFoundException extends LocationException {

  public LocationNotFoundException(double latitude, double longitude) {
    super(ErrorCode.LOCATION_NOT_FOUND);
    addDetail("latitude", latitude);
    addDetail("longitude", longitude);
  }
}
