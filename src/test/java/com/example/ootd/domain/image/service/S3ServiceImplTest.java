package com.example.ootd.domain.image.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.ootd.domain.image.entity.Image;
import com.example.ootd.domain.image.service.impl.S3ServiceImpl;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@ExtendWith(MockitoExtension.class)
public class S3ServiceImplTest {

  @Mock
  private S3Client s3Client;

  @InjectMocks
  private S3ServiceImpl s3Service;

  @BeforeEach
  void setUp() {
    // @Value 필드 수동 주입
    ReflectionTestUtils.setField(s3Service, "bucket", "test-bucket");
    ReflectionTestUtils.setField(s3Service, "region", "ap-northeast-2");
  }

  @Nested
  @DisplayName("save() - 사진 저장")
  class saveTest {

    @Test
    @DisplayName("S3에 사진 저장 성공")
    void saveSuccess() throws IOException {

      // given
      String content = "저장할 사진";
      MockMultipartFile file = new MockMultipartFile(
          "file", "image.jpg", "image/jpeg", content.getBytes()
      );

      // mock 객체 호출 시 PutObjectRequest 타입의 인자를 잡아낼 준비
      ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(
          PutObjectRequest.class);

      // when
      Image image = s3Service.save(file);

      // then
      assertThat(image).isNotNull();
      assertThat(image.getFileName()).contains("image.jpg");
      assertThat(image.getUrl()).contains("https://test-bucket.s3.ap-northeast-2.amazonaws.com");
      verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));
      assertThat(requestCaptor.getValue().bucket()).isEqualTo("test-bucket");
    }

    @Test
    @DisplayName("S3에 사진 저장 실패")
    void saveFailed() throws IOException {

      // given
      MultipartFile file = mock(MultipartFile.class);
      given(file.getOriginalFilename()).willReturn("image.jpg");
      given(file.getContentType()).willReturn("image/jpeg");
      given(file.getInputStream()).willThrow(new IOException("Stream Error"));

      // when & then
      assertThatThrownBy(() -> s3Service.save(file))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("S3 파일 업로드 실패");
      verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }
  }

  @Nested
  @DisplayName("delete() - 사진 삭제")
  class deleteTest {

    @Test
    @DisplayName("S3에 저장된 사진 삭제 성공")
    void deleteSuccess() {

      // given
      String fileName = "test.jpg";

      // when
      s3Service.delete(fileName);

      // then
      ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(
          DeleteObjectRequest.class);
      verify(s3Client).deleteObject(captor.capture());
      assertThat(captor.getValue().bucket()).isEqualTo("test-bucket");
      assertThat(captor.getValue().key()).isEqualTo(fileName);
    }

    @Test
    @DisplayName("S3에 저장된 사진 삭제 실패")
    void deleteFailed() {

      // given
      String fileName = "test.jpg";
      given(s3Client.deleteObject(any(DeleteObjectRequest.class)))
          .willThrow(new RuntimeException("AWS Error"));

      // when & then
      assertThatThrownBy(() -> s3Service.delete(fileName))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("S3 파일 삭제 실패");
    }
  }
}
