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
public class StudyPostUpdateResponse {

  private LocalDateTime updatedAt;

  public static StudyPostUpdateResponse fromEntity(StudyPost studyPost) {
    return StudyPostUpdateResponse.builder()
        .updatedAt(studyPost.getUpdatedAt())
        .build();
  }
}
