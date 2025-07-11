package com.example.ootd.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

  // Auth 에러
  AUTHENTICATION_FAILED("인증에 실패하였습니다."),

  // Clothes 에러
  CLOTHES_NOT_FOUND("옷을 찾을 수 없습니다."),
  ATTRIBUTE_NOT_FOUND("속성 정의를 찾을 수 없습니다."),
  ATTRIBUTE_DETAIL_NOT_FOUND("속성 내용을 찾을 수 없습니다."),

  // Feed 에러
  FEED_NOT_FOUND("피드를 찾을 수 없습니다."),
  FEED_LIKE_NOT_FOUND("피드 좋아요 정보를 찾을 수 없습니다."),

  // Image 에러
  IMAGE_NOT_FOUND("이미지를 찾을 수 없습니다."),

  // Follow 에러
  ALREADY_FOLLOWED_USER("이미 팔로우 중인 사용자입니다."),
  FOLLOWER_NOT_FOUND("팔로워를 찾을 수 없습니다."),
  FOLLOWEE_NOT_FOUND("팔로우 대상 사용자를 찾을 수 없습니다."),
  FOLLOW_USER_NOT_FOUND("사용자를 찾을 수 없습니다."),
  FOLLOW_NOT_FOUND("팔로우를 찾을 수 없습니다."),

  // Server 에러
  INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다."),
  INVALID_REQUEST("잘못된 요청입니다."),

  // User 에러
  USER_NOT_FOUND("사용자 이메일을 찾을 수 없습니다.");

  private final String message;

  ErrorCode(String message) {
    this.message = message;
  }
}
