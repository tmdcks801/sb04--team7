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
  ATTRIBUTE_NAME_DUPLICATE("동일한 속성이 이미 존재합니다."),

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

  // Weather 에러
  WEATHER_NOT_FOUND("날씨 정보를 찾을 수 없습니다."),
  WEATHER_API_ERROR("날씨 API 호출에 실패했습니다."),
  WEATHER_DATA_INSUFFICIENT("충분한 날씨 데이터가 없습니다."),
  INVALID_COORDINATES("잘못된 좌표값입니다."),
  WEATHER_REGION_NOT_FOUND("해당 지역의 날씨 정보를 찾을 수 없습니다."),

  // Location 에러
  LOCATION_NOT_FOUND("위치 정보를 찾을 수 없습니다."),
  LOCATION_API_ERROR("위치 API 호출에 실패했습니다."),
  LOCATION_COORDINATE_OUT_OF_RANGE("한국 외 지역 좌표입니다."),
  LOCATION_REGION_INFO_INSUFFICIENT("지역 정보가 부족합니다."),

  // Server 에러
  INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다."),
  INVALID_REQUEST("잘못된 요청입니다."),

  // User 에러
  USER_NOT_FOUND("사용자 이메일을 찾을 수 없습니다."),

  //메세지 에러
  FAIL_SEND_MESSAGE("메세지 전송 실패"),
  FAIL_GET_MESSAGE("메세지 조회 실패"),

  //알림 에러
  FAIL_CREATE_NOTIFICATION("알림 생성 실패 "),
  FAIL_CREATE_BULK_NOTIFICATION("벌크 알림 생성 실패 "),
  NOT_FOUND_NOTIFICATION("알람 찾기 실패"),
  FAIL_READ_NOTIFICATION("알람 읽기 실패"),
  FAIL_GET_PAGINATION_NOTIFICATION("알람 페이지네이션 오류"),

  //sse
  FAIL_SSE_SUBSCRIBE("SSE 구독 실패"),
  FAIL_SSE_PUSH("SSE 푸시 실패"),
  FAIL_SSE_HEARTBEAT("SSE 연명 실패"),
  FAIL_SSE_ADD("더하기 실패"),
  FAIL_SSE_REMOVE("SSE 구독 취소 실패");


  private final String message;

  ErrorCode(String message) {
    this.message = message;
  }
}
