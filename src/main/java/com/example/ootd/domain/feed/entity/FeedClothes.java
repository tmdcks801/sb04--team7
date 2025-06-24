package com.example.ootd.domain.feed.entity;

import com.example.ootd.domain.clothes.entity.Clothes;
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
 * 피드_옷 중간 엔티티
 */
@Entity
@Table(name = "feed_clothes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedClothes {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(nullable = false, updatable = false)
  private UUID id;  // pk

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "feed_id", nullable = false)
  private Feed feed;  // 피드

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "clothes_id", nullable = false)
  private Clothes clothes;  // 옷

  @Builder
  public FeedClothes(Feed feed, Clothes clothes) {
    this.feed = feed;
    this.clothes = clothes;
  }
}
