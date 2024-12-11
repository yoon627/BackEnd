package com.devonoff.exception;

import static com.devonoff.type.ErrorCode.BAD_REQUEST;
import static com.devonoff.type.ErrorCode.INTERNAL_SERVER_ERROR;

import com.devonoff.common.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
    log.error("{} is occurred", e.getErrorCode());
    return ResponseEntity.status(e.getErrorCode().getStatus())
        .body(
            ErrorResponse.builder()
                .errorCode(e.getErrorCode())
                .errorMessage(e.getErrorMessage())
                .build()
        );
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(Exception e) {
    log.error("MethodArgumentNotValidException Error is occurred", e);
    return ResponseEntity.status(BAD_REQUEST.getStatus())
        .body(
            ErrorResponse.builder()
                .errorCode(BAD_REQUEST)
                .errorMessage(BAD_REQUEST.getDescription())
                .build()
        );
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    log.error("Error is occurred", e);
    return ResponseEntity.status(INTERNAL_SERVER_ERROR.getStatus())
        .body(
            ErrorResponse.builder()
                .errorCode(INTERNAL_SERVER_ERROR)
                .errorMessage(INTERNAL_SERVER_ERROR.getDescription())
                .build()
        );
  }

}
