package com.devonoff.domain.qnapost.dto;

import com.devonoff.domain.qnapost.entity.QnaComment;
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
public class QnaCommentResponse {

  private Long id;
  private Long postId;
  private Boolean isSecret;
  private String content;
  private UserDto user;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private List<QnaReplyDto> replies;

  public static QnaCommentResponse fromEntity(QnaComment qnaComment) {
    return QnaCommentResponse.builder()
        .id(qnaComment.getId())
        .postId(qnaComment.getQnaPost().getId())
        .isSecret(qnaComment.getIsSecret())
        .content(qnaComment.getContent())
        .user(UserDto.fromEntity(qnaComment.getUser()))
        .createdAt(qnaComment.getCreatedAt())
        .updatedAt(qnaComment.getUpdatedAt())
        .replies(qnaComment.getReplies().stream().map(QnaReplyDto::fromEntity).toList())
        .build();
  }
}
