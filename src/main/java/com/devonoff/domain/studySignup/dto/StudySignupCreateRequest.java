package com.devonoff.domain.studySignup.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudySignupCreateRequest {

  @NotNull(message = "스터디 모집글 ID는 필수입니다.")
  private Long studyPostId;

  @NotNull(message = "사용자 ID는 필수입니다.")
  private Long userId;
}
