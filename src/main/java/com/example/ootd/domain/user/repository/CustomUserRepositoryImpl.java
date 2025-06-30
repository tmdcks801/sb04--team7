package com.example.ootd.domain.user.repository;

import com.example.ootd.domain.user.QUser;
import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.dto.UserSearchCondition;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;


@Repository
@RequiredArgsConstructor
public class CustomUserRepositoryImpl implements CustomUserRepository{


  private final JPAQueryFactory jpaQueryFactory;
  QUser user = QUser.user;

  @Override
  public List<User> searchUserOfCondition(UserSearchCondition condition) {

    int limitPlusOne = condition.limit() + 1;
    BooleanBuilder booleanBuilder = new BooleanBuilder();

    if(condition.emailLike() != null && !condition.emailLike().isBlank()) {
      booleanBuilder.and(user.email.containsIgnoreCase(condition.emailLike()));
    }

    if(condition.roleEqual() != null) {
      booleanBuilder.and(user.role.eq(condition.roleEqual()));
    }

    if(condition.locked()) {
      booleanBuilder.and(user.isLocked.eq(true));
    }


    // TODO : 정렬 잘 되는지 확인 필요
    if(condition.idAfter() != null && !condition.idAfter().toString().isBlank()){
      if("DESCENDING".equalsIgnoreCase(condition.sortDirection())){
        booleanBuilder.and(user.id.lt(condition.idAfter()));
      }else{
        booleanBuilder.and(user.id.gt(condition.idAfter()));
      }
    }

    // TODO : createdAt 외에 다른 조건 있는지 확인 필요
    if(condition.cursor() != null && !condition.cursor().isBlank()){
      if("DESCENDING".equalsIgnoreCase(condition.sortDirection())){
        booleanBuilder.and(user.createdAt.lt(LocalDateTime.parse(condition.cursor())));
      } else {
        booleanBuilder.and(user.createdAt.gt(LocalDateTime.parse(condition.cursor())));
      }
    }

    boolean ascending = !"DESCENDING".equalsIgnoreCase(condition.sortDirection());

    return jpaQueryFactory
        .selectFrom(user)
        .where(booleanBuilder)
        .orderBy(getSortExpression(condition.sortBy(), ascending))
        .limit(limitPlusOne)
        .fetch();
  }

  private OrderSpecifier<?> getSortExpression(String sortBy, boolean ascending) {
    if ("email".equalsIgnoreCase(sortBy)) {
      return ascending ? user.email.asc() : user.email.desc();
    } else if ("name".equalsIgnoreCase(sortBy)) {
      return ascending ? user.name.asc() : user.name.desc();
    } else if ("createdAt".equalsIgnoreCase(sortBy)) {
      return ascending ? user.createdAt.asc() : user.createdAt.desc();
    } else {
      return ascending ? user.createdAt.asc() : user.createdAt.desc();
    }
  }

}
