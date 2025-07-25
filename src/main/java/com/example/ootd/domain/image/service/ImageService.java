package com.example.ootd.domain.image.service;

import com.example.ootd.domain.image.entity.Image;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

/**
 * 사진 관리
 */
public interface ImageService {

  // 사진 저장
  Image upload(MultipartFile image);

  // 사진 조회
  String read(UUID id);

  // 사진 삭제
  void delete(UUID id);
}