package com.example.ootd.domain.feed.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
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

  // TODO: user 추가 후 주석 해제
//  @ManyToOne(fetch = FetchType.LAZY)
//  @JoinColumn(name = "user_id", columnDefinition = "uuid")
//  private User user;  // 피드 작성자

  @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<FeedClothes> feedClothes;  // 피드에 등록된 옷(중간 테이블)

  @Column
  private String content; // 설명

  @Column
  private long likeCount; // 좋아요 수

  @Column
  private int commentCount; // 댓글 수

  @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<FeedComment> comments; // 댓글 목록

  @Column(nullable = false, updatable = false)
  @CreatedDate
  private LocalDateTime createdAt;  // 생성일

  @Column
  @LastModifiedDate
  private LocalDateTime updatedAt;  // 수정일

  // TODO: user 추가 후 주석 해제
//  @Builder
//  public Feed(User user, List<FeedClothes> feedClothes, String content) {
//    this.user = user;
//    this.feedClothes = feedClothes;
//    this.content = content;
//    this.likeCount = 0;
//    this.commentCount = 0;
//    this.comments = new ArrayList<>();
//  }
}
