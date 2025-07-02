package com.example.ootd.batch;

import com.example.ootd.batch.dto.RegionInfo;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RegionCsvReader {

  @Value("${weather.region.csv.path:classpath:regions.csv}")
  private Resource csvResource;

  public List<RegionInfo> readAllRegions() {
    List<RegionInfo> regions = new ArrayList<>();

    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(csvResource.getInputStream(), StandardCharsets.UTF_8))) {

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

    } catch (Exception e) {
      log.error("Error reading regions from CSV", e);
      throw new RuntimeException("Failed to read regions from CSV", e);
    }

    log.info("Loaded {} regions from CSV", regions.size());
    return regions;
  }
}