package com.example.ootd.domain.recommend.service;

import com.example.ootd.domain.recommend.dto.ScoredClothesDto;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class ClothesCalculator {

  private static final double BASE_SCORE = 40.0;

  public double calculateScore(ScoredClothesDto clothes) {
    double feltTemperature = calculateFeltTemperature(clothes);

    double baseScore = BASE_SCORE;
    double temperatureScore = calculateTemperatureScore(clothes, feltTemperature);
    double rainAdjustment = calculateRainAdjustment(clothes);
    double seasonScore = calculateSeasonScore(clothes);

    return baseScore + temperatureScore  + rainAdjustment + seasonScore;
  }

  private double calculateSeasonScore(ScoredClothesDto clothes) {
    String currentSeason = getCurrentSeason();
    if(clothes.season() != null && clothes.season().contains(currentSeason)) {
      return 15.0;
    }
    return 0.0;
  }

  private String getCurrentSeason() {
    int month = LocalDate.now().getMonthValue();
    return switch (month) {
      case 3, 4, 5 -> "봄";
      case 6, 7, 8, 9 -> "여름";
      case 10, 11 -> "가일";
      case 12, 1, 2 -> "겨울";
      default -> "봄";
    };
  }

  private double calculateFeltTemperature(ScoredClothesDto clothes) {
    return clothes.temperatureCurrent()
        - (clothes.windSpeed() * 0.8)
        + (clothes.humidityCurrent() * 0.04)
        + (clothes.precipitationAmount() > 0 ? -2 : 0)
        + clothes.temperatureSensitivity();
  }

  private double calculateTemperatureScore(ScoredClothesDto clothes, double feltTemperature) {
    if (feltTemperature >= 30) {
      return calculateHotScore(clothes);
    } else if (feltTemperature >= 20) {
      return calculateWarmScore(clothes);
    } else if (feltTemperature >= 10) {
      return calculateCoolScore(clothes);
    } else {
      return calculateColdScore(clothes);
    }
  }

  private double calculateHotScore(ScoredClothesDto clothes) {
    double thicknessScore = switch (clothes.thickness() != null ? clothes.thickness() : "") {
      case "얇음" -> 25;
      case "약간 얇음" -> 19;
      default -> 0;
    };

    double colorScore = switch (clothes.color() != null ? clothes.color() : "") {
      case "화이트", "베이지", "크림", "그레이" -> 11;
      case "블랙", "네이비", "다크 그레이" -> 4;
      default -> 1;
    };

    return thicknessScore + colorScore;
  }

  private double calculateWarmScore(ScoredClothesDto clothes) {
    double thicknessScore = switch (clothes.thickness() != null ? clothes.thickness() : "") {
      case "얇음" -> 25;
      case "약간 얇음" -> 17;
      case "약간 두꺼움" -> 3;
      default -> 0;
    };

    double colorScore = switch (clothes.color() != null ? clothes.color() : "") {
      case "화이트", "베이지", "크림", "그레이" -> 12;
      case "블랙", "네이비", "다크 그레이" -> 8;
      default -> 6;
    };

    return thicknessScore + colorScore;
  }

  private double calculateCoolScore(ScoredClothesDto clothes) {
    double thicknessScore = switch (clothes.thickness() != null ? clothes.thickness() : "") {
      case "약간 두꺼움" -> 24;
      case "약간 얇음" -> 22;
      case "두꺼움" -> 16;
      default -> 0;
    };

    double colorScore = switch (clothes.color() != null ? clothes.color() : "") {
      case "블랙", "네이비", "다크 그레이" -> 13;
      case "화이트", "베이지", "크림", "그레이" -> 11;
      default -> 8;
    };

    return thicknessScore + colorScore;
  }

  private double calculateColdScore(ScoredClothesDto clothes) {
    double thicknessScore = switch (clothes.thickness() != null ? clothes.thickness() : "") {
      case "두꺼움" -> 24;
      case "약간 두꺼움" -> 17;
      case "약간 얇음" -> 1;
      default -> 0;
    };

    double colorScore = switch (clothes.color() != null ? clothes.color() : "") {
      case "블랙", "네이비", "다크 그레이" -> 13;
      case "화이트", "베이지", "크림", "그레이" -> 7;
      default -> 4;
    };

    return thicknessScore + colorScore;
  }

  private double calculateRainAdjustment(ScoredClothesDto clothes) {
    if (clothes.precipitationAmount() <= 0) {
      return 0;
    }

    return switch (clothes.color() != null ? clothes.color() : "") {
      case "블랙", "네이비", "다크 그레이" -> 3;
      case "화이트", "베이지", "크림", "그레이" -> -3;
      default -> 0;
    };
  }
}