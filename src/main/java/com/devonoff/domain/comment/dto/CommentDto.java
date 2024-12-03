package com.devonoff.domain.comment.dto;

import com.devonoff.domain.comment.entity.Comment;
import com.devonoff.domain.user.dto.UserDto;
import com.devonoff.type.PostType;
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
public class CommentDto {

  private Long id;
  private PostType postType;
  private Long postId;
  private Boolean isSecret;
  private String content;
  private String createdAt;
  private String updatedAt;
  private UserDto user; // User 정보를 UserDto로 포함

  /**
   * Entity -> DTO 변환
   *
   * @param comment Comment 엔티티
   * @return CommentDto
   */
  public static CommentDto fromEntity(Comment comment) {
    return CommentDto.builder()
        .id(comment.getId())
        .postType(comment.getPostType())
        .postId(comment.getPostId())
        .isSecret(comment.getIsSecret())
        .content(comment.getContent())
        .createdAt(comment.getCreatedAt() != null ? comment.getCreatedAt().toString() : null)
        .updatedAt(comment.getUpdatedAt() != null ? comment.getUpdatedAt().toString() : null)
        .user(comment.getUser() != null ? UserDto.fromEntity(comment.getUser()) : null)
        .build();
  }

  /**
   * DTO -> Entity 변환
   *
   * @param commentDto CommentDto
   * @return Comment
   */
  public static Comment toEntity(CommentDto commentDto) {
    return Comment.builder()
        .id(commentDto.getId())
        .postType(commentDto.getPostType())
        .postId(commentDto.getPostId())
        .isSecret(commentDto.getIsSecret())
        .content(commentDto.getContent())
        .user(commentDto.getUser() != null ? UserDto.toEntity(commentDto.getUser()) : null)
        .build();
  }
}