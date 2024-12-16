package com.devonoff.domain.infosharepost.dto;

import com.devonoff.domain.infosharepost.entity.InfoShareComment;
import com.devonoff.domain.user.dto.UserDto;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InfoShareCommentDto {

  private Long id;
  private Long postId;
  private Boolean isSecret;
  private String content;
  private UserDto user;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static InfoShareCommentDto fromEntity(InfoShareComment infoShareComment) {
    return InfoShareCommentDto.builder()
        .id(infoShareComment.getId())
        .postId(infoShareComment.getInfoSharePost().getId())
        .isSecret(infoShareComment.getIsSecret())
        .content(infoShareComment.getContent())
        .user(UserDto.fromEntity(infoShareComment.getUser()))
        .createdAt(infoShareComment.getCreatedAt())
        .updatedAt(infoShareComment.getUpdatedAt())
        .build();
  }
}
