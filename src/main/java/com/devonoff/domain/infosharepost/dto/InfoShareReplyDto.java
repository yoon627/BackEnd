package com.devonoff.domain.infosharepost.dto;

import com.devonoff.domain.infosharepost.entity.InfoShareReply;
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
public class InfoShareReplyDto {

  private Long id;
  private Long commentId;
  private Boolean isSecret;
  private String content;
  private UserDto user;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static InfoShareReplyDto fromEntity(InfoShareReply infoShareReply) {
    return InfoShareReplyDto.builder()
        .id(infoShareReply.getId())
        .commentId(infoShareReply.getComment().getId())
        .isSecret(infoShareReply.getIsSecret())
        .content(infoShareReply.getContent())
        .user(UserDto.fromEntity(infoShareReply.getUser()))
        .createdAt(infoShareReply.getCreatedAt())
        .updatedAt(infoShareReply.getUpdatedAt())
        .build();
  }
}
