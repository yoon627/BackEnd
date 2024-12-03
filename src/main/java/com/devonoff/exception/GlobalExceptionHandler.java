package com.devonoff.exception;

import static com.devonoff.type.ErrorCode.INTERNAL_SERVER_ERROR;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<String> handleCustomException(CustomException e) {
    log.error("{} is occurred", e.getErrorCode());
    return ResponseEntity.status(e.getErrorCode().getStatus()).body(e.getErrorMessage());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<String> handleException(Exception e) {
    log.error("Error is occurred", e);
    return ResponseEntity.status(INTERNAL_SERVER_ERROR.getStatus())
        .body(INTERNAL_SERVER_ERROR.getDescription());
  }

}
