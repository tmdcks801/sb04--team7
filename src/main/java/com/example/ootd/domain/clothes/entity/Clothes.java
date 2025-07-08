package com.example.ootd.domain.clothes.entity;

import com.example.ootd.domain.image.entity.Image;
import com.example.ootd.domain.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 옷 엔티티
 */
@Entity
@Table(name = "clothes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Clothes {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(nullable = false, updatable = false)
  private UUID id;  // pk

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", columnDefinition = "uuid")
  private User user;  // 옷 등록자

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "image_id", columnDefinition = "uuid")
  private Image image;  // 사진

  @Column(nullable = false)
  private String name;  // 옷 이름

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ClothesType type;

  @BatchSize(size = 100)
  @OneToMany(mappedBy = "clothes", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private Set<ClothesAttribute> clothesAttributes = new HashSet<>();

  @Column(nullable = false, updatable = false)
  @CreatedDate
  private LocalDateTime createdAt;  // 등록일

  @Column
  @LastModifiedDate
  private LocalDateTime updatedAt;  // 수정일

  @Builder
  public Clothes(User user, Image image, String name, ClothesType type) {
    this.user = user;
    this.image = image;
    this.name = name;
    this.type = type;
  }

  public void updateName(String name) {
    if (this.name.equals(name)) {
      return;
    }
    this.name = name;
  }

  public void updateType(ClothesType type) {
    if (this.type.equals(type)) {
      return;
    }
    this.type = type;
  }

  public void updateImage(Image image) {
    this.image = image;
  }

  public void addClothesAttribute(ClothesAttribute clothesAttribute) {
    this.clothesAttributes.add(clothesAttribute);
  }

  public void removeClothesAttribute(ClothesAttribute clothesAttribute) {

    if (!this.clothesAttributes.contains(clothesAttribute)) {
      return;
    }
    this.clothesAttributes.remove(clothesAttribute);
  }
}