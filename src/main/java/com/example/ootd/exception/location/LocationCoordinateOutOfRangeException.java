package com.example.ootd.exception.location;

import com.example.ootd.exception.ErrorCode;

// 좌표가 한국 범위를 벗어날 때
public class LocationCoordinateOutOfRangeException extends LocationException {

  public LocationCoordinateOutOfRangeException(double latitude, double longitude) {
    super(ErrorCode.LOCATION_COORDINATE_OUT_OF_RANGE);
    addDetail("latitude", latitude);
    addDetail("longitude", longitude);
  }
}
