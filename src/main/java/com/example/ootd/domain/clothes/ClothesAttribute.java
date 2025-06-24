package com.example.ootd.domain.clothes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 옷 속성 엔티티
 */
@Entity
@Table(name = "clothes_attributes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClothesAttribute {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(nullable = false, updatable = false)
  private UUID id;  // pk

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "clothes_id", columnDefinition = "uuid")
  private Clothes clothes;  // 옷
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "attribute_id", columnDefinition = "uuid")
  private Attribute attribute;  // 속성

  @Column(nullable = false)
  private String value; // 옷에 해당하는 속성 내용

  @Builder
  public ClothesAttribute(Clothes clothes, Attribute attribute, String value) {
    this.clothes = clothes;
    this.attribute = attribute;
    this.value = value;
  }
}
