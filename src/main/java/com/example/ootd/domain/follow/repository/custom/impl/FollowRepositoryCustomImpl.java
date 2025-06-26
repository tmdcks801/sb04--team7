package com.example.ootd.domain.follow.repository.custom.impl;

import com.example.ootd.domain.follow.entity.QFollow;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class FollowRepositoryCustomImpl {

  private final JPAQueryFactory queryFactory;
  QFollow follow = QFollow.follow;

  //TODO : 프로토콜 사이트에 구현이 아직 안되어 있음. 사이트에 추가되면 구현
}
