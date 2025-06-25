package com.example.ootd.domain.image.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.ootd.domain.image.entity.Image;
import com.example.ootd.domain.image.service.S3Service;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

  private final AmazonS3 amazonS3;

  @Value("${ootd.storage.s3.bucket}")
  private String bucket;

  @Override
  public Image save(MultipartFile file) {

    try {
      String originalFileName = file.getOriginalFilename();
      String encodedFileName = URLEncoder.encode(originalFileName, StandardCharsets.UTF_8);
      String fileName = UUID.randomUUID() + "_" + encodedFileName;

      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentLength(file.getSize());
      metadata.setContentType(file.getContentType());

      amazonS3.putObject(bucket, fileName, file.getInputStream(), metadata);

      Image image = new Image(amazonS3.getUrl(bucket, fileName).toString(), fileName);

      return image;

    } catch (IOException e) {
      log.error("S3 업로드 실패", e);
      throw new RuntimeException("S3 파일 업로드 실패", e);
    }
  }

  @Override
  public void delete(String fileName) {
    try {

      amazonS3.deleteObject(bucket, fileName);

    } catch (Exception e) {
      log.error("S3 삭제 실패", e);
      throw new RuntimeException("S3 파일 삭제 실패", e);
    }
  }
}
