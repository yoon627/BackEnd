package com.devonoff.domain.studySignup.dto;

import com.devonoff.domain.studySignup.entity.StudySignup;
import com.devonoff.type.StudySignupStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudySignupDto {

  private Long signupId;
  private Long userId;
  private String nickName;
  private StudySignupStatus status;

  public static StudySignupDto fromEntity(StudySignup signup) {
    return StudySignupDto.builder()
        .signupId(signup.getId())
        .userId(signup.getUser().getId())
        .nickName(signup.getUser().getNickname())
        .status(signup.getStatus())
        .build();
  }
}