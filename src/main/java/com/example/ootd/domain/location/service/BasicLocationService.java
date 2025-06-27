package com.example.ootd.domain.location.service;

import com.example.ootd.domain.location.api.KakaoApiClient;
import com.example.ootd.domain.location.api.KakaoApiResponse;
import com.example.ootd.domain.location.dto.WeatherAPILocation;
import com.example.ootd.domain.location.util.GridGpsConverter;
import com.example.ootd.domain.location.util.GridGpsConverter.LatXLngY;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicLocationService implements LocationService {

  private final KakaoApiClient kakaoApiClient;

  @Override
  public WeatherAPILocation getGridAndLocation(double latitude, double longitude) {

    LatXLngY grid = GridGpsConverter.convertGRID_GPS(GridGpsConverter.MODE_GRID, latitude,
        longitude);

    List<KakaoApiResponse.Document> documents = kakaoApiClient.getAdministrativeRegions(longitude,
        latitude);

    List<String> locationNames = documents.stream()
        .findFirst()
        .map(doc -> List.of(
            doc.region_1depth_name(),
            doc.region_2depth_name(),
            doc.region_3depth_name(),
            doc.region_4depth_name()
        ))
        .orElse(List.of("알 수 없음"));

    return new WeatherAPILocation(
        latitude,
        longitude,
        grid.x.intValue(),
        grid.y.intValue(),
        locationNames
    );
  }
}
