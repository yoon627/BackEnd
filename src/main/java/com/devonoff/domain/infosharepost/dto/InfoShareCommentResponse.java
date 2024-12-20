package com.devonoff.domain.infosharepost.dto;

import com.devonoff.domain.infosharepost.entity.InfoShareComment;
import com.devonoff.domain.user.dto.UserDto;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InfoShareCommentResponse {

  private Long id;
  private Long postId;
  private Boolean isSecret;
  private String content;
  private UserDto user;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private List<InfoShareReplyDto> replies;

  public static InfoShareCommentResponse fromEntity(InfoShareComment infoShareComment) {
    return InfoShareCommentResponse.builder()
        .id(infoShareComment.getId())
        .postId(infoShareComment.getInfoSharePost().getId())
        .isSecret(infoShareComment.getIsSecret())
        .content(infoShareComment.getContent())
        .user(UserDto.fromEntity(infoShareComment.getUser()))
        .createdAt(infoShareComment.getCreatedAt())
        .updatedAt(infoShareComment.getUpdatedAt())
        .replies(infoShareComment.getReplies().stream().map(InfoShareReplyDto::fromEntity).toList())
        .build();
  }
}
