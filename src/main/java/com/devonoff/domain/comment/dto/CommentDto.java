package com.devonoff.domain.comment.dto;

import com.devonoff.domain.comment.entity.Comment;
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
public class CommentDto {


  private PostType postType;
  private Long postId;
  private Boolean isSecret;
  private String content;
  private LocalDateTime createdAt; // 생성일 추가
  private LocalDateTime updatedAt;

  /**
   * Entity -> DTO 변환
   */
  public static CommentDto fromEntity(Comment comment) {

    return CommentDto.builder()
        .postType(comment.getPostType())
        .postId(comment.getPostId())
        .isSecret(comment.getIsSecret())
        .content(comment.getContent())
        .createdAt(comment.getCreatedAt()) // 생성일 설정
        .updatedAt(comment.getUpdatedAt()) // 수정일 설정
        .build();
  }

  /**
   * DTO -> Entity 변환
   */
  public static Comment toEntity(CommentDto commentDto) {
    return Comment.builder()
        .postType(commentDto.getPostType())
        .postId(commentDto.getPostId())
        .isSecret(commentDto.getIsSecret())
        .content(commentDto.getContent())
        .build();
  }
}