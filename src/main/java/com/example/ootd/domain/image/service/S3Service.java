package com.example.ootd.domain.image.service;

import com.example.ootd.domain.image.entity.Image;
import org.springframework.web.multipart.MultipartFile;

/**
 * S3 파일 관리 서비스
 */
public interface S3Service {

  // 파일 업로드
  Image save(MultipartFile file);

  // 파일 삭제
  void delete(String fileName);
}
