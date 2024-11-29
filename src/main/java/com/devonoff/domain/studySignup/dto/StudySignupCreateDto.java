package com.devonoff.domain.studySignup.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class StudySignupCreateDto {

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Request {
    @NotNull(message = "스터디 모집글 ID는 필수입니다.")
    private Long studyPostId;

    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Response {
    private String message;
  }
}