package com.example.ootd.exception;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {

    log.error("예상치 못한 오류 발생: {}", e.getMessage(), e);
    ErrorResponse errorResponse = new ErrorResponse(e);

    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(errorResponse);
  }

  @ExceptionHandler(OotdException.class)
  public ResponseEntity<ErrorResponse> handleOotdException(OotdException e) {

    log.error("커스텀 예외 발생: message={}", e.getMessage(), e);
    HttpStatus status = determineHttpStatus(e);
    ErrorResponse response = new ErrorResponse(e);

    return ResponseEntity
        .status(status)
        .body(response);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(
      MethodArgumentNotValidException e) {

    log.error("요청 유효성 검사 실패: {}", e.getMessage());

    Map<String, Object> validationErrors = new HashMap<>();
    e.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      validationErrors.put(fieldName, errorMessage);
    });

    ErrorResponse response = new ErrorResponse(
        "VALIDATION_ERROR",
        "요청 데이터 유효성 검사에 실패했습니다.",
        validationErrors
    );

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(response);
  }

  private HttpStatus determineHttpStatus(
      OotdException e) { // ErrorCode 생성시 HttpStatus 를 같이 넘기는 것이 더 깔끔한것 같음 (개선안)
    ErrorCode errorCode = e.getErrorCode();
    return switch (errorCode) {
      case AUTHENTICATION_FAILED -> HttpStatus.UNAUTHORIZED;
      case CLOTHES_NOT_FOUND, FEED_NOT_FOUND, IMAGE_NOT_FOUND, FOLLOWER_NOT_FOUND,
           FOLLOWEE_NOT_FOUND, FOLLOW_USER_NOT_FOUND, FOLLOW_NOT_FOUND, ATTRIBUTE_NOT_FOUND,
           USER_NOT_FOUND, ATTRIBUTE_DETAIL_NOT_FOUND, FEED_LIKE_NOT_FOUND, FAIL_GET_MESSAGE,
           WEATHER_NOT_FOUND, WEATHER_REGION_NOT_FOUND, LOCATION_NOT_FOUND,
           NOT_FOUND_NOTIFICATION -> HttpStatus.NOT_FOUND;
      case ALREADY_FOLLOWED_USER, ATTRIBUTE_NAME_DUPLICATE, FEED_LIKE_DUPLICATE ->
          HttpStatus.CONFLICT;
      case INVALID_COORDINATES, LOCATION_COORDINATE_OUT_OF_RANGE,
           WEATHER_DATA_INSUFFICIENT, LOCATION_REGION_INFO_INSUFFICIENT -> HttpStatus.BAD_REQUEST;
      case WEATHER_API_ERROR, LOCATION_API_ERROR -> HttpStatus.BAD_GATEWAY;
      case INTERNAL_SERVER_ERROR, INVALID_REQUEST, FAIL_SEND_MESSAGE, FAIL_CREATE_NOTIFICATION
      , FAIL_CREATE_BULK_NOTIFICATION, FAIL_READ_NOTIFICATION, FAIL_GET_PAGINATION_NOTIFICATION,
           FAIL_SSE_SUBSCRIBE, FAIL_SSE_PUSH, FAIL_SSE_HEARTBEAT, FAIL_SSE_ADD, FAIL_SSE_REMOVE ->
          HttpStatus.INTERNAL_SERVER_ERROR;
    };
  }
}
