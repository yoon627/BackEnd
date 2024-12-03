package com.devonoff.domain.studyPost.dto;

import com.devonoff.domain.studyPost.entity.StudyPost;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyPostCreateResponse {

  private LocalDateTime createdAt;

  public static StudyPostCreateResponse fromEntity(StudyPost studyPost) {
    return StudyPostCreateResponse.builder()
        .createdAt(studyPost.getCreatedAt())
        .build();
  }
}
