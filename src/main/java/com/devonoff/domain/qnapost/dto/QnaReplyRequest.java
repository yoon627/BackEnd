package com.devonoff.domain.qnapost.dto;

import com.devonoff.domain.qnapost.entity.QnaComment;
import com.devonoff.domain.qnapost.entity.QnaReply;
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
public class QnaReplyRequest {

  @NotNull
  private Boolean isSecret;
  @NotBlank
  private String content;

  public static QnaReply toEntity(
      User user, QnaComment qnaComment, QnaReplyRequest qnaReplyRequest
  ) {
    return QnaReply.builder()
        .comment(qnaComment)
        .isSecret(qnaReplyRequest.getIsSecret())
        .content(qnaReplyRequest.getContent())
        .user(user)
        .build();
  }
}
