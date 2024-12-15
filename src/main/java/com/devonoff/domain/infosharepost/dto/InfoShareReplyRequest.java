package com.devonoff.domain.infosharepost.dto;

import com.devonoff.domain.infosharepost.entity.InfoShareComment;
import com.devonoff.domain.infosharepost.entity.InfoSharePost;
import com.devonoff.domain.infosharepost.entity.InfoShareReply;
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
public class InfoShareReplyRequest {

  @NotNull
  private Boolean isSecret;
  @NotBlank
  private String content;

  public static InfoShareReply toEntity(
      User user, InfoShareComment infoShareComment, InfoShareReplyRequest infoShareReplyRequest
  ) {
    return InfoShareReply.builder()
        .comment(infoShareComment)
        .isSecret(infoShareReplyRequest.getIsSecret())
        .content(infoShareReplyRequest.getContent())
        .user(user)
        .build();
  }
}
