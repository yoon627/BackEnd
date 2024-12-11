package com.devonoff.domain.reply.dto;

import com.devonoff.domain.reply.entity.Reply;
import com.devonoff.domain.user.dto.UserDto;
import com.devonoff.type.PostType;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ReplyResponse {

  private Long id;
  private Boolean isSecret;
  private Long postId;
  private PostType postType;
  private Long commentsId;
  private String content;
  private UserDto userDto;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static ReplyResponse fromEntity(Reply reply) {
    return ReplyResponse.builder()
        .id(reply.getId())
        .isSecret(reply.getIsSecret())
        .postId(reply.getPostId())
        .postType(reply.getPostType())
        .commentsId(reply.getComment().getId())
        .content(reply.getContent())
        .userDto(UserDto.fromEntity(reply.getUser()))  // User 엔티티에서 UserDto로 변환
        .createdAt(reply.getCreatedAt())
        .updatedAt(reply.getUpdatedAt())
        .build();
  }
}