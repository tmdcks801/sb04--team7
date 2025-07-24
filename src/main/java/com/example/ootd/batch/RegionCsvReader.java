package com.example.ootd.batch;

import com.example.ootd.batch.dto.RegionInfo;
import com.example.ootd.domain.image.service.S3Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RegionCsvReader {

  private final S3Service s3Service;

  @Value("${weather.region.csv.s3-key:region-csv/region.csv}")
  private String csvS3Key;

  public List<RegionInfo> readAllRegions() {
    List<RegionInfo> regions = new ArrayList<>();

    try {
      // S3에서 파일 읽기 시도
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(s3Service.getFileInputStream(csvS3Key), StandardCharsets.UTF_8))) {

        regions = parseRegionsFromReader(reader);
        log.info("Successfully loaded {} regions from S3 CSV: {}", regions.size(), csvS3Key);

      } catch (Exception s3Exception) {
        log.warn("Failed to read from S3, falling back to classpath resource: {}",
            s3Exception.getMessage());

        // S3 실패 시 classpath의 파일로 fallback
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(getClass().getClassLoader().getResourceAsStream("regions.csv"),
                StandardCharsets.UTF_8))) {

          if (reader == null) {
            throw new RuntimeException("Neither S3 nor classpath regions.csv file found");
          }

          regions = parseRegionsFromReader(reader);
          log.info("Successfully loaded {} regions from classpath fallback", regions.size());
        }
      }

    } catch (Exception e) {
      log.error("Error reading regions from both S3 and classpath", e);
      throw new RuntimeException("Failed to read regions from both S3 and classpath", e);
    }

    return regions;
  }

  private List<RegionInfo> parseRegionsFromReader(BufferedReader reader) throws Exception {
    List<RegionInfo> regions = new ArrayList<>();
    String line;
    boolean isFirstLine = true;

    while ((line = reader.readLine()) != null) {
      if (isFirstLine) {
        isFirstLine = false;
        continue; // 헤더 스킵
      }

      String[] parts = line.split(",");
      if (parts.length >= 3) {
        RegionInfo region = new RegionInfo();
        region.setRegionName(parts[0].trim());
        region.setNx(Integer.parseInt(parts[1].trim()));
        region.setNy(Integer.parseInt(parts[2].trim()));
        regions.add(region);
      }
    }

    return regions;
  }
}