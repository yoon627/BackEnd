package com.devonoff.domain.infosharepost.dto;

import com.devonoff.domain.infosharepost.entity.InfoSharePost;
import com.devonoff.domain.infosharepost.entity.InfoShareComment;
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
public class InfoShareCommentRequest {

  @NotNull
  private Boolean isSecret;
  @NotBlank
  private String content;

  public static InfoShareComment toEntity (
      User user, InfoSharePost infoSharePost,
      InfoShareCommentRequest infoShareCommentRequest
  ) {
    return InfoShareComment.builder()
        .infoSharePost(infoSharePost)
        .isSecret(infoShareCommentRequest.getIsSecret())
        .content(infoShareCommentRequest.getContent())
        .user(user)
        .build();
  }
}
