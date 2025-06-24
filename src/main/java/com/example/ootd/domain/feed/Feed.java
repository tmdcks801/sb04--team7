package com.example.ootd.domain.feed;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
 * 피드 엔티티
 */
@Entity
@Table(name = "feeds")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Feed {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false)
  private UUID id;  // pk

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", columnDefinition = "uuid")
  private User user;  // 피드 작성자

  @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<FeedClothes> feedClothes;  // 피드에 등록된 옷

  @Column
  private String content; // 설명

  @Column
  private int feedLike; // 좋아요 수

  @Column(nullable = false, updatable = false)
  @CreatedDate  
  private LocalDateTime createdAt;  // 생성일

  @Column
  @LastModifiedDate
  private LocalDateTime updatedAt;  // 수정일

  @Builder
  public Feed(User user, List<FeedClothes> feedClothes, String content) {
    this.user = user;
    this.feedClothes = feedClothes;
    this.content = content;
    this.feedLike = 0;
  }
}
