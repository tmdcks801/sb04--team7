package com.example.ootd.domain.feed.repository.custom.impl;

import com.example.ootd.domain.feed.dto.request.FeedSearchCondition;
import com.example.ootd.domain.feed.entity.Feed;
import com.example.ootd.domain.feed.entity.QFeed;
import com.example.ootd.domain.feed.repository.custom.CustomFeedRepository;
import com.example.ootd.domain.user.QUser;
import com.example.ootd.domain.weather.entity.QWeather;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class CustomFeedRepositoryImpl implements CustomFeedRepository {

  private final JPAQueryFactory jpaQueryFactory;
  private final QFeed qFeed = QFeed.feed;
  private final QUser qUser = QUser.user;
  private final QWeather qWeather = QWeather.weather;

  @Override
  public List<Feed> findByCondition(FeedSearchCondition condition) {

    return jpaQueryFactory
        .select(qFeed).distinct()
        .from(qFeed)
        .leftJoin(qFeed.user, qUser).fetchJoin()
        .leftJoin(qFeed.weather, qWeather).fetchJoin()
        .where(
            getWhere(condition),
            cursorCondition(condition)
        )
        .orderBy(
            getOrderBy(condition.sortBy(), condition.sortDirection())
        )
        .limit(condition.limit() + 1)
        .fetch();
  }

  @Override
  public long countByCondition(FeedSearchCondition condition) {

    Long count = jpaQueryFactory
        .select(qFeed.count())
        .from(qFeed)
        .where(getWhere(condition))
        .fetchOne();

    if (count == null) {
      return 0;
    } else {
      return count;
    }
  }

  /**
   * where절
   */
  // 키워드, 하늘 상태, 강수량, 작성자 id로 검색
  private BooleanBuilder getWhere(FeedSearchCondition condition) {

    BooleanBuilder where = new BooleanBuilder();

    if (condition.keywordLike() != null) {
      where.and(qFeed.content.contains(condition.keywordLike()));
    }
    if (condition.skyStatusEqual() != null) {
      where.and(qFeed.weather.skyStatus.eq(condition.skyStatusEqual()));
    }
    if (condition.precipitationTypeEqual() != null) {
      where.and(
          qFeed.weather.precipitation.precipitationType.eq(condition.precipitationTypeEqual()));
    }
    if (condition.authorIdEqual() != null) {
      where.and(qFeed.user.id.eq(condition.authorIdEqual()));
    }

    return where;
  }

  /**
   * 커서 페이지네이션
   */
  // 커서(cursor) 세팅
  private BooleanExpression cursorCondition(FeedSearchCondition condition) {

    boolean isDesc = "DESCENDING".equalsIgnoreCase(condition.sortDirection());

    if (StringUtils.hasText(condition.cursor())) {
      switch (condition.sortBy()) {
        case "createdAt":
          LocalDateTime cursorCreatedAt = LocalDateTime.parse(condition.cursor());
          if (isDesc) {
            // 내림차순일 경우 cursor값이 작거나, 같되 idAfter가 작아야 함
            return qFeed.createdAt.lt(cursorCreatedAt)
                .or(qFeed.createdAt.eq(cursorCreatedAt)
                    .and(qFeed.id.lt(condition.idAfter())));
          } else {
            // 오름차순일 경우 cursor값이 크거나, 같되 idAfter가 커야 함
            return qFeed.createdAt.gt(cursorCreatedAt)
                .or(qFeed.createdAt.eq(cursorCreatedAt)
                    .and(qFeed.id.gt(condition.idAfter())));
          }
        case "likeCount":
          long cursorLikeCount = Long.parseLong(condition.cursor());
          if (isDesc) {
            // 내림차순일 경우 cursor값이 작거나, 같되 idAfter가 작아야 함
            return qFeed.likeCount.lt(cursorLikeCount)
                .or(qFeed.likeCount.eq(cursorLikeCount)
                    .and(qFeed.id.lt(condition.idAfter())));
          } else {
            // 오름차순일 경우 cursor값이 크거나, 같되 idAfter가 커야 함
            return qFeed.likeCount.gt(cursorLikeCount)
                .or(qFeed.likeCount.eq(cursorLikeCount)
                    .and(qFeed.id.gt(condition.idAfter())));
          }
      }
    }

    return null;
  }

  /**
   * orderBy절
   */
  // 생성일 or 좋아요 수
  private OrderSpecifier<?>[] getOrderBy(String sortBy, String sortDirection) {

    boolean isDesc = "DESCENDING".equalsIgnoreCase(sortDirection);

    switch (sortBy) {
      case "createdAt":  // 생성일
        if (isDesc) {
          return new OrderSpecifier<?>[]{qFeed.createdAt.desc(), qFeed.id.desc()};
        } else {
          return new OrderSpecifier<?>[]{qFeed.createdAt.asc(), qFeed.id.asc()};
        }
      case "likeCount":
        if (isDesc) {
          return new OrderSpecifier<?>[]{qFeed.likeCount.desc(), qFeed.id.desc()};
        } else {
          return new OrderSpecifier<?>[]{qFeed.likeCount.asc(), qFeed.id.asc()};
        }
      default:
        return new OrderSpecifier<?>[]{qFeed.createdAt.desc(), qFeed.id.desc()};  // 기본 정렬 최신순
    }
  }
}
