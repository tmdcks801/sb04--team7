package com.example.ootd.domain.clothes;

import com.example.ootd.converter.StringListConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

  @Column(columnDefinition = "text")
  @Convert(converter = StringListConverter.class)
  private List<String> details; // 속성 내용

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
}
