package com.example.ootd.domain.image.event;

import static org.mockito.Mockito.verify;

import com.example.ootd.domain.image.entity.Image;
import com.example.ootd.domain.image.repository.ImageRepository;
import com.example.ootd.domain.image.service.S3Service;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ActiveProfiles("test")
@DataJpaTest
@Import({S3Service.class, ImageDeleteListener.class})
@EntityScan(basePackageClasses = Image.class)
@EnableJpaRepositories(basePackageClasses = ImageRepository.class)
public class ImageDeleteListenerTest {

  @Autowired
  private ImageRepository imageRepository;

  @MockitoBean
  private S3Service s3Service;

  @Nested
  @DisplayName("preRemove() - 이미지 삭제 시 S3에서도 삭제")
  class preRemoveTest {

    @Test
    @DisplayName("삭제 성공")
    void preRemoveSuccess() {

      // given
      Image image = Image.builder()
          .fileName("test.jpg")
          .url("https://test-bucket.s3.region.amazonaws.com/test.jpg")
          .build();
      imageRepository.save(image);

      // when
      imageRepository.delete(image);

      // then
      verify(s3Service).delete("test.jpg");
    }
  }
}

