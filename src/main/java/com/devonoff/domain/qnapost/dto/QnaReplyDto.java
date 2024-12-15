package com.devonoff.domain.qnapost.dto;

import com.devonoff.domain.qnapost.entity.QnaReply;
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
public class QnaReplyDto {

  private Long id;
  private Long commentId;
  private Boolean isSecret;
  private String content;
  private UserDto user;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static QnaReplyDto fromEntity(QnaReply qnaReply) {
    return QnaReplyDto.builder()
        .id(qnaReply.getId())
        .commentId(qnaReply.getComment().getId())
        .isSecret(qnaReply.getIsSecret())
        .content(qnaReply.getContent())
        .user(UserDto.fromEntity(qnaReply.getUser()))
        .createdAt(qnaReply.getCreatedAt())
        .updatedAt(qnaReply.getUpdatedAt())
        .build();
  }
}
