package com.devonoff.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseDto {

  private String message;

  public static ResponseDto getResponseBody(String message) {
    return ResponseDto.builder()
        .message(message)
        .build();
  }
}
