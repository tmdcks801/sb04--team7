package com.example.ootd.domain.image.service.impl;

import com.example.ootd.domain.image.entity.Image;
import com.example.ootd.domain.image.service.S3Service;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

  private final S3Client s3Client;

  @Value("${ootd.storage.s3.bucket}")
  private String bucket;

  @Value("${ootd.storage.s3.region}")
  private String region;

  @Override
  public Image save(MultipartFile file) {

    try {
      String originalFileName = file.getOriginalFilename();
      String fileName = UUID.randomUUID() + "_" + originalFileName;

      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
          .bucket(bucket)
          .key(fileName)
          .contentType(file.getContentType())
          .build();

      s3Client.putObject(putObjectRequest,
          RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

      // TODO: presigned url로 변경?
      String url = "https://" + bucket + ".s3." + region + ".amazonaws.com/" + fileName;
      Image image = new Image(url, fileName);

      return image;

    } catch (IOException e) {
      log.error("S3 업로드 실패", e);
      throw new RuntimeException("S3 파일 업로드 실패", e);
    }
  }

  @Override
  public void delete(String fileName) {

    try {
      DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
          .bucket(bucket)
          .key(fileName)
          .build();

      s3Client.deleteObject(deleteObjectRequest);

    } catch (Exception e) {
      log.error("S3 삭제 실패", e);
      throw new RuntimeException("S3 파일 삭제 실패", e);
    }
  }
}
