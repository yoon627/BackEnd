package com.devonoff.domain.comment.dto;

import com.devonoff.domain.comment.entity.Comment;
import com.devonoff.domain.user.dto.UserDto;
import com.devonoff.type.PostType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
//댓글반환
public class CommentResponse {

  private Long id;
  private PostType postType;
  private Long postId;
  private Boolean isSecret;
  private String content;
  private UserDto user;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;


  public static CommentResponse fromEntity(Comment comment) {
    return CommentResponse.builder()
        .id(comment.getId())
        .postType(comment.getPostType())
        .postId(comment.getPostId())
        .isSecret(comment.getIsSecret())
        .content(comment.getContent())
        .createdAt(comment.getCreatedAt())
        .updatedAt(comment.getUpdatedAt())
        .user(comment.getUser() != null ? UserDto.fromEntity(comment.getUser()) : null)
        .build();
  }
}