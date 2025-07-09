package com.example.ootd.domain.image.event;

import com.example.ootd.domain.image.entity.Image;
import com.example.ootd.domain.image.service.S3Service;
import jakarta.persistence.PreRemove;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImageDeleteListener {

  private final S3Service s3Service;

  // 이미지 삭제 시 s3에 저장된 사진도 삭제
  @PreRemove
  public void preRemove(Image image) {
    s3Service.delete(image.getFileName());
  }
}
