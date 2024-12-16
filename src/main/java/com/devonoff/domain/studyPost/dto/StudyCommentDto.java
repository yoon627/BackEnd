package com.devonoff.domain.studyPost.dto;

import com.devonoff.domain.studyPost.entity.StudyComment;
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
public class StudyCommentDto {

  private Long id;
  private Long postId;
  private Boolean isSecret;
  private String content;
  private UserDto user;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static StudyCommentDto fromEntity(StudyComment studyComment) {
    return StudyCommentDto.builder()
        .id(studyComment.getId())
        .postId(studyComment.getStudyPost().getId())
        .isSecret(studyComment.getIsSecret())
        .content(studyComment.getContent())
        .user(UserDto.fromEntity(studyComment.getUser()))
        .createdAt(studyComment.getCreatedAt())
        .updatedAt(studyComment.getUpdatedAt())
        .build();
  }
}
