package com.example.ootd.domain.clothes.service;

import com.example.ootd.domain.clothes.dto.data.RecommendationDto;
import java.util.UUID;

public interface RecommendService {

  // 옷 추천
  RecommendationDto recommend(UUID weatherId);
}
