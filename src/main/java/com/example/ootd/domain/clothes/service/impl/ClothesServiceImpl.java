package com.example.ootd.domain.clothes.service.impl;

import com.example.ootd.domain.clothes.dto.data.ClothesDto;
import com.example.ootd.domain.clothes.dto.data.RecommendationDto;
import com.example.ootd.domain.clothes.dto.request.ClothesCreateRequest;
import com.example.ootd.domain.clothes.dto.request.ClothesSearchRequest;
import com.example.ootd.domain.clothes.dto.request.ClothesUpdateRequest;
import com.example.ootd.domain.clothes.entity.Clothes;
import com.example.ootd.domain.clothes.mapper.ClothesMapper;
import com.example.ootd.domain.clothes.repository.ClothesRepository;
import com.example.ootd.domain.clothes.service.ClothesService;
import com.example.ootd.domain.image.entity.Image;
import com.example.ootd.domain.image.service.ImageService;
import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.repository.UserRepository;
import com.example.ootd.dto.PageResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ClothesServiceImpl implements ClothesService {

  private final ClothesRepository clothesRepository;
  private final ImageService imageService;
  private final UserRepository userRepository;
  private final ClothesMapper clothesMapper;

  @Override
  public ClothesDto create(ClothesCreateRequest request, MultipartFile image) {

    Image clothesImage = imageService.upload(image);
    User user = userRepository.findById(request.ownerId())
        .orElseThrow(); // TODO: null 처리

    Clothes clothes = Clothes.builder()
        .image(clothesImage)
        .user(user)
        .name(request.name())
        .type(request.type())
        .build();
    clothesRepository.save(clothes);

    ClothesDto clothesDto = clothesMapper.toDto(clothes);

    return clothesDto;
  }

  @Override
  public ClothesDto update(ClothesUpdateRequest request, MultipartFile image) {



    return null;
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<ClothesDto> findByCondition(ClothesSearchRequest request) {
    return null;
  }

  @Override
  public void delete(UUID clothesId) {

  }

  @Override
  @Transactional(readOnly = true)
  public RecommendationDto recommend(UUID weatherId) {
    return null;
  }
}
