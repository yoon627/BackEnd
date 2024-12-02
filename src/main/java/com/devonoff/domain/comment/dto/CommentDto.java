package com.devonoff.domain.comment.dto;

import com.devonoff.type.PostType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
  private Long id;
  private PostType postType;
  private Long postId;
  private Boolean isSecret;
  private String content;
  private String createdAt;
  private String updatedAt;
  private Long userId;
}