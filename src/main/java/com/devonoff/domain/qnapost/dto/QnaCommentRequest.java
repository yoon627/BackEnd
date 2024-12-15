package com.devonoff.domain.qnapost.dto;

import com.devonoff.domain.qnapost.entity.QnaComment;
import com.devonoff.domain.qnapost.entity.QnaPost;
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
public class QnaCommentRequest {

  @NotNull
  private Boolean isSecret;
  @NotBlank
  private String content;

  public static QnaComment toEntity (
      User user, QnaPost qnaPost,
      QnaCommentRequest qnaCommentRequest
  ) {
    return QnaComment.builder()
        .qnaPost(qnaPost)
        .isSecret(qnaCommentRequest.getIsSecret())
        .content(qnaCommentRequest.getContent())
        .user(user)
        .build();
  }
}
