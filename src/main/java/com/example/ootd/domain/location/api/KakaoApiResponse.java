package com.example.ootd.domain.location.api;

import java.util.List;

public record KakaoApiResponse(
    List<Document> documents
) {

  public record Document(
      String region_type,
      String code,
      String address_name,
      String region_1depth_name,
      String region_2depth_name,
      String region_3depth_name,
      String region_4depth_name,
      double x,
      double y
  ) {

  }
}
