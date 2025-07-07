package com.example.ootd.exception.location;

import com.example.ootd.exception.ErrorCode;

// 지역 정보가 부족할 때
public class LocationRegionInfoInsufficientException extends LocationException {

  public LocationRegionInfoInsufficientException(String receivedInfo) {
    super(ErrorCode.LOCATION_REGION_INFO_INSUFFICIENT);
    addDetail("receivedInfo", receivedInfo);
  }
}
