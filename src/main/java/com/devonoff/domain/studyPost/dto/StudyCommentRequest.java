package com.devonoff.domain.studyPost.dto;

import com.devonoff.domain.studyPost.entity.StudyComment;
import com.devonoff.domain.studyPost.entity.StudyPost;
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
public class StudyCommentRequest {

  @NotNull
  private Boolean isSecret;
  @NotBlank
  private String content;

  public static StudyComment toEntity (
      User user, StudyPost studyPost,
      StudyCommentRequest studyCommentRequest
  ) {
    return StudyComment.builder()
        .studyPost(studyPost)
        .isSecret(studyCommentRequest.getIsSecret())
        .content(studyCommentRequest.getContent())
        .user(user)
        .build();
  }
}
