package com.devonoff.domain.studyPost.dto;

import com.devonoff.domain.studyPost.entity.StudyComment;
import com.devonoff.domain.studyPost.entity.StudyReply;
import com.devonoff.domain.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyReplyRequest {

  @NotNull
  private Boolean isSecret;
  @NotBlank
  private String content;

  public static StudyReply toEntity(
      User user, StudyComment studyComment, StudyReplyRequest studyReplyRequest
  ) {
    return StudyReply.builder()
        .comment(studyComment)
        .isSecret(studyReplyRequest.getIsSecret())
        .content(studyReplyRequest.getContent())
        .user(user)
        .build();
  }
}
