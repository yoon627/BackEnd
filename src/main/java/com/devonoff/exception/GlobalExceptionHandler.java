package com.devonoff.exception;

import static com.devonoff.type.ErrorCode.INTERNAL_SERVER_ERROR;

import com.devonoff.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(CustomException.class)
  public ErrorResponse handleCustomException(CustomException e) {
    log.error("{} is occurred", e.getErrorCode());
    return new ErrorResponse(e.getErrorCode(), e.getErrorMessage());
  }

  @ExceptionHandler(Exception.class)
  public ErrorResponse handleException(Exception e) {
    log.error("Error is occurred", e);
    return new ErrorResponse(INTERNAL_SERVER_ERROR,
        INTERNAL_SERVER_ERROR.getDescription());
  }

}
