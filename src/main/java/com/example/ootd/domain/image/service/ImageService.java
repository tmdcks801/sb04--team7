package com.example.ootd.domain.image.service;

import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

/**
 * 사진 관리
 */
public interface ImageService {

  // 사진 저장
  String upload(MultipartFile image);

  // 사진 조회
  String read(UUID id);
}