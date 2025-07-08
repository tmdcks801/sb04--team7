package com.example.ootd.domain.clothes.entity;

import com.example.ootd.converter.StringListConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 속성 엔티티
 */
@Entity
@Table(name = "attributes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Attribute {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(nullable = false, updatable = false)
  private UUID id;  // pk

  @Column(nullable = false, unique = true)
  private String name;  // 속성 이름

  @Column(columnDefinition = "text", name = "details")
  private String detailsRaw; // 속성 내용, DB 저장용 문자열

  @Transient  // JPA가 해당 필드를 DB 컬럼과 매핑하지 않도록 하는 어노테이션
  private List<String> details; // 속성 내용, 실제 로직에서 사용할 리스트

  @Column(nullable = false, updatable = false)
  @CreatedDate
  private LocalDateTime createdAt;  // 생성일

  @Column
  @LastModifiedDate
  private LocalDateTime updatedAt;  // 수정일

  @Builder
  public Attribute(String name, List<String> details) {
    this.name = name;
    this.details = details;
  }

  public void updateName(String name) {
    this.name = name;
  }

  public void updateDetails(List<String> detailList) {
    this.details = detailList;
    this.detailsRaw = StringListConverter.serialize(details);
  }

  @PostLoad // DB에서 엔티티 로드 후 실행, DB 조회 후 details 초기화
  private void postLoad() {
    this.details = StringListConverter.deserialize(detailsRaw);
  }

  @PrePersist // 엔티티 저장 전에 실행, DB 저장 직전 detailsRaw 세팅
  private void persistDetails() {
    this.detailsRaw = StringListConverter.serialize(details);
  }

  // 속성 내용 중 vlaue가 존재할 경우 true 반환
  public boolean isValidValue(String value) {
    return details != null && details.contains(value);
  }
}
