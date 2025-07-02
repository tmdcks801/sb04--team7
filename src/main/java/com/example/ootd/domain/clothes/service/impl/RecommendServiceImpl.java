package com.example.ootd.domain.clothes.service.impl;


import com.example.ootd.domain.clothes.dto.data.RecommendationDto;
import com.example.ootd.domain.clothes.service.RecommendService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RecommendServiceImpl implements RecommendService {

  @Override
  @Transactional(readOnly = true)
  public RecommendationDto recommend(UUID weatherId) {
    return null;
  }
}
