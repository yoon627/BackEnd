package com.devonoff.domain.studyPost.dto;

import com.devonoff.domain.studyPost.entity.StudyReply;
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
public class StudyReplyDto {

  private Long id;
  private Long commentId;
  private Boolean isSecret;
  private String content;
  private UserDto user;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static StudyReplyDto fromEntity(StudyReply studyReply) {
    return StudyReplyDto.builder()
        .id(studyReply.getId())
        .commentId(studyReply.getComment().getId())
        .isSecret(studyReply.getIsSecret())
        .content(studyReply.getContent())
        .user(UserDto.fromEntity(studyReply.getUser()))
        .createdAt(studyReply.getCreatedAt())
        .updatedAt(studyReply.getUpdatedAt())
        .build();
  }
}
