package com.example.ootd.domain.clothes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;
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

  @Column(nullable = false, name = "\"value\"")
  private String value; // 옷에 해당하는 속성 내용

  @Builder
  public ClothesAttribute(Clothes clothes, Attribute attribute, String value) {
    this.clothes = clothes;
    this.attribute = attribute;
    this.value = value;
  }

  public void updateValue(String value) {
    this.value = value;
  }

  // 속성 하나당 하나의 값만 저장
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ClothesAttribute that = (ClothesAttribute) o;
    return Objects.equals(clothes, that.clothes) && Objects.equals(attribute,
        that.attribute);
  }

  @Override
  public int hashCode() {
    return Objects.hash(clothes, attribute);
  }
}
