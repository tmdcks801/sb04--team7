package com.example.ootd.domain.image.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.example.ootd.TestEntityFactory;
import com.example.ootd.domain.image.entity.Image;
import com.example.ootd.domain.image.repository.ImageRepository;
import com.example.ootd.domain.image.service.impl.ImageServiceImpl;
import com.example.ootd.exception.image.ImageNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class ImageServiceImplTest {

  @Mock
  private S3Service s3Service;

  @Mock
  private ImageRepository imageRepository;

  @InjectMocks
  private ImageServiceImpl imageService;

  // 테스트용 Image 객체
  private final Image testImage = TestEntityFactory.createImageWithoutId("1");

  @Nested
  @DisplayName("upload() - 사진 업로드")
  class uploadTest {

    @Test
    @DisplayName("업로드 및 DB 저장 성공")
    void uploadSuccess() {

      // given
      MultipartFile file = mock(MultipartFile.class);
      given(s3Service.save(file)).willReturn(testImage);

      // when
      Image savedImage = imageService.upload(file);

      // then
      assertThat(savedImage).isNotNull();
      assertThat(savedImage.getFileName()).isEqualTo(testImage.getFileName());
      verify(s3Service).save(file);
      verify(imageRepository).save(testImage);
    }

    @Test
    @DisplayName("이미지가 null이면 null 반환")
    void uploadNullImageReturnsNull() {

      // when
      Image result = imageService.upload(null);

      // then
      assertThat(result).isNull();
    }
  }

  @Nested
  @DisplayName("read() - 사진 조회")
  class readTest {

    @Test
    @DisplayName("저장된 사진 url 조회 성공")
    void readSuccess() {

      // given
      given(imageRepository.findById(testImage.getId())).willReturn(Optional.of(testImage));

      // when
      String url = imageService.read(testImage.getId());

      // then
      assertThat(url).isEqualTo(testImage.getUrl());
      verify(imageRepository).findById(testImage.getId());
    }

    @Test
    @DisplayName("사진 없을 경우 예외 발생")
    void readImageNotFound() {

      // given
      given(imageRepository.findById(testImage.getId())).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> imageService.read(testImage.getId()))
          .isInstanceOf(ImageNotFoundException.class)
          .hasMessageContaining("이미지를 찾을 수 없습니다.");
      verify(imageRepository).findById(testImage.getId());
    }
  }

  @Nested
  @DisplayName("delete() - 사진 삭제")
  class deleteTest {

    @Test
    @DisplayName("삭제 성공")
    void deleteSuccess() {

      // given
      given(imageRepository.findById(testImage.getId())).willReturn(Optional.of(testImage));

      // when
      imageService.delete(testImage.getId());

      // then
      verify(imageRepository).findById(testImage.getId());
      verify(s3Service).delete(testImage.getFileName());
      verify(imageRepository).delete(testImage);
    }

    @Test
    @DisplayName("id에 해당하는 이미지가 없을 경우 예외처리")
    void deleteImageNotFound() {

      // given
      given(imageRepository.findById(testImage.getId())).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> imageService.delete(testImage.getId()))
          .isInstanceOf(ImageNotFoundException.class)
          .hasMessageContaining("이미지를 찾을 수 없습니다.");
      verify(imageRepository).findById(testImage.getId());
    }
  }
}
