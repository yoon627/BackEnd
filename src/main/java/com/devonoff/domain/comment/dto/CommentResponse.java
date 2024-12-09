package com.devonoff.domain.comment.dto;

import com.devonoff.domain.comment.entity.Comment;
import com.devonoff.domain.reply.dto.ReplyResponse;
import com.devonoff.domain.user.dto.UserDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
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
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
// 응답값
public class CommentResponse {

  private Long id;
  private String postType;
  private Long postId;
  private Boolean isSecret;
  private String content;
  private UserDto user; // User 정보를 포함

  @JsonFormat(pattern = "yyyy-MM-dd:HH:mm:ss")
  private LocalDateTime createdAt;

  @JsonFormat(pattern = "yyyy-MM-dd:HH:mm:ss")
  private LocalDateTime updatedAt;

  private List<ReplyResponse> replies; // 대댓글 리스트 포함

  /**
   * CommentDto -> CommentResponse 변환
   *
   * @param comment comment
   * @return CommentResponse
   */
  public static CommentResponse fromEntity(Comment comment) {
    return CommentResponse.builder()
        .id(comment.getId())
        .postType(comment.getPostType().toString())
        .postId(comment.getPostId())
        .isSecret(comment.getIsSecret())
        .content(comment.getContent())
        .user(UserDto.fromEntity(comment.getUser()))
        .createdAt(comment.getCreatedAt())
        .updatedAt(comment.getUpdatedAt())
        .replies(comment.getReplies() != null
            ? comment.getReplies().stream()
            .map(ReplyResponse::fromEntity)
            .collect(Collectors.toList())
            : null) // 대댓글 리스트 변환
        .build();
  }
}
